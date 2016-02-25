package edu.jhu.hopkinspd.security;
import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

import edu.jhu.hopkinspd.GlobalApp;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Base64;

public class AESCrypt
{
	private static final String JCE_EXCEPTION_MESSAGE = "Please make sure "
		+ "\"Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files\" "
		+ "(http://java.sun.com/javase/downloads/index.jsp) is installed on your JRE.";
	private static final String RANDOM_ALG = "SHA1PRNG";
	private static final String DIGEST_ALG = "SHA-256";
	private static final String HMAC_ALG = "HmacSHA256";
	private static final String CRYPT_ALG = "AES";
	private static final String CRYPT_TRANS = "AES/CBC/NoPadding";
	private static final int KEY_SIZE = 32;
	private static final int IV_BLOCK_SIZE = 16;
	private static final int SHA_SIZE = 32;

	private Cipher cipher;
	private Mac hmac;
	private SecureRandom random;
	private MessageDigest digest;
	private IvParameterSpec ivSpec1;
	private SecretKeySpec aesKey1;
	private IvParameterSpec ivSpec2;
	private SecretKeySpec aesKey2;
	private GlobalApp app;

	/**
	 * Generates a pseudo-random byte array.
	 * @return pseudo-random byte array of <tt>len</tt> bytes.
	 */
	protected byte[] generateRandomBytes(int len)
	{
		byte[] bytes = new byte[len];
		random.nextBytes(bytes);
		return bytes;
	}


	/**
	 * SHA256 digest over given byte array and random bytes.<br>
	 * <tt>bytes.length</tt> * <tt>num</tt> random bytes are added to the digest.
	 * <p>
	 * The generated hash is saved back to the original byte array.<br>
	 * Maximum array size is {@link #SHA_SIZE} bytes.
	 */
	protected void digestRandomBytes(byte[] bytes, int num)
	{
		assert bytes.length <= SHA_SIZE;

		digest.reset();
		digest.update(bytes);
		for (int i = 0; i < num; i++)
		{
			random.nextBytes(bytes);
			digest.update(bytes);
		}
		System.arraycopy(digest.digest(), 0, bytes, 0, bytes.length);
	}


	/**
	 * IV based on this computer's MAC.
	 * <p>
	 * This IV is used to encrypt IV 2 and AES key 2 in the file.
	 * @return IV.
	 */
	protected byte[] generateIv1()
	{
		byte[] iv = new byte[IV_BLOCK_SIZE];	// Initialized to zeros - Java language standard
		
		WifiManager wifiManager = (WifiManager)app.getSystemService(Context.WIFI_SERVICE);
		String[] macAddressParts = wifiManager.getConnectionInfo().getMacAddress().split(":");
		byte[] mac = new byte[macAddressParts.length];
		for (int i = 0; i < mac.length; i++)
		{
		    Integer hex = Integer.parseInt(macAddressParts[i], 16);
		    mac[i] = hex.byteValue();
		}
		
		System.arraycopy(mac, 0, iv, 8, mac.length);
		return iv;
	}


	/**
	 * Generates an AES key starting with an IV and applying the supplied user password.
	 * <p>
	 * This AES key is used to encrypt IV 2 and AES key 2.
	 * @return AES key of {@link #KEY_SIZE} bytes.
	 */
	protected byte[] generateAESKey1(byte[] iv, byte[] password)
	{
		byte[] aesKey = new byte[KEY_SIZE];
		System.arraycopy(iv, 0, aesKey, 0, iv.length);
		for (int i = 0; i < 8192; i++)
		{
			digest.reset();
			digest.update(aesKey);
			digest.update(password);
			aesKey = digest.digest();
		}
		return aesKey;
	}


	/**
	 * Generates the random IV used to encrypt file contents.
	 * @return IV 2.
	 */
	protected byte[] generateIV2()
	{
		byte[] iv = generateRandomBytes(IV_BLOCK_SIZE);
		digestRandomBytes(iv, 256);
		return iv;
	}


	/**
	 * Generates the random AES key used to encrypt file contents.
	 * @return AES key of {@link #KEY_SIZE} bytes.
	 */
	protected byte[] generateAESKey2()
	{
		byte[] aesKey = generateRandomBytes(KEY_SIZE);
		digestRandomBytes(aesKey, 32);
		return aesKey;
	}


	/**
	 * Utility method to read bytes from a stream until the given array is fully filled.
	 * @throws IOException if the array can't be filled.
	 */
	protected void readBytes(InputStream in, byte[] bytes) throws IOException
	{
		if (in.read(bytes) != bytes.length)
		{
			throw new IOException("Unexpected end of file");
		}
	}

	/**
	 * Builds an object to encrypt files.
	 * @throws GeneralSecurityException if the platform does not support the required cryptographic methods.
	 */
	public AESCrypt(GlobalApp app) throws GeneralSecurityException
	{
		try
		{
			this.app = app;
			random = SecureRandom.getInstance(RANDOM_ALG);
			digest = MessageDigest.getInstance(DIGEST_ALG);
			cipher = Cipher.getInstance(CRYPT_TRANS);
			hmac = Mac.getInstance(HMAC_ALG);
			ivSpec1 = new IvParameterSpec(generateIv1());
		}
		catch (GeneralSecurityException e)
		{
			throw new GeneralSecurityException(JCE_EXCEPTION_MESSAGE, e);
		}
	}

	public String generateAESStringKey(String password)
	{
		try
		{
			byte[] passBytes = password.getBytes("UTF-16LE");
			SecretKeySpec key = new SecretKeySpec(generateAESKey1(ivSpec1.getIV(), passBytes), CRYPT_ALG);
			return Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public SecretKeySpec aesKeyStringToKeySpec(String aesKeyString)
	{
		byte[] encodedKey = Base64.decode(aesKeyString, Base64.DEFAULT);
		return new SecretKeySpec(encodedKey, "AES");
	}
	
	/**
	 * The file at <tt>fromPath</tt> is encrypted and saved at <tt>toPath</tt> location.
	 * <p>
	 * <tt>version</tt> can be either 1 or 2.
	 * @throws IOException when there are I/O errors.
	 * @throws GeneralSecurityException if the platform does not support the required cryptographic methods.
	 */
	public void encrypt(int version, String fromPath, String toPath, String aesStringKey)
	throws IOException, GeneralSecurityException
	{
		InputStream in = null;
		OutputStream out = null;
		try
		{
			in = new BufferedInputStream(new FileInputStream(fromPath));
			out = new BufferedOutputStream(new FileOutputStream(toPath));
			encrypt(version, in, out, aesStringKey);
		}
		finally
		{
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		}
	}

	/**
	 * The input stream is encrypted and saved to the output stream.
	 * <p>
	 * <tt>version</tt> can be either 1 or 2.<br>
	 * None of the streams are closed.
	 * @throws IOException when there are I/O errors.
	 * @throws GeneralSecurityException if the platform does not support the required cryptographic methods.
	 */
	public void encrypt(int version, InputStream in, OutputStream out, String aesStringKey)
	throws IOException, GeneralSecurityException {
		try {
			byte[] text = null;

			aesKey1 = aesKeyStringToKeySpec(aesStringKey);
			ivSpec2 = new IvParameterSpec(generateIV2());
			aesKey2 = new SecretKeySpec(generateAESKey2(), CRYPT_ALG);

			out.write("AES".getBytes("UTF-8"));	// Heading.
			out.write(version);	// Version.
			out.write(0);	// Reserved.
			if (version == 2)
			{
				out.write(0);
				out.write(0);
			}
			out.write(ivSpec1.getIV());	// Initialization Vector.

			text = new byte[IV_BLOCK_SIZE + KEY_SIZE];
			cipher.init(Cipher.ENCRYPT_MODE, aesKey1, ivSpec1);
			cipher.update(ivSpec2.getIV(), 0, IV_BLOCK_SIZE, text);
			cipher.doFinal(aesKey2.getEncoded(), 0, KEY_SIZE, text, IV_BLOCK_SIZE);
			out.write(text);	// Crypted IV and key.

			hmac.init(new SecretKeySpec(aesKey1.getEncoded(), HMAC_ALG));
			text = hmac.doFinal(text);
			out.write(text);	// HMAC from previous cyphertext.

			cipher.init(Cipher.ENCRYPT_MODE, aesKey2, ivSpec2);
			hmac.init(new SecretKeySpec(aesKey2.getEncoded(), HMAC_ALG));
			text = new byte[IV_BLOCK_SIZE];
			int len, last = 0;
			while ((len = in.read(text)) > 0)
			{
				cipher.update(text, 0, IV_BLOCK_SIZE, text);
				hmac.update(text);
				out.write(text);	// Crypted file data block.
				last = len;
			}
			last &= 0x0f;
			out.write(last);	// Last block size mod 16.

			text = hmac.doFinal();
			out.write(text);	// HMAC from previous cyphertext.
		}
		catch (InvalidKeyException e)
		{
			throw new GeneralSecurityException(JCE_EXCEPTION_MESSAGE, e);
		}
	}

}
