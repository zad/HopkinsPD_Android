/*
 * Copyright (c) 2015 Johns Hopkins University. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the
 *   distribution.
 * - Neither the name of the copyright holder nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.jhu.hopkinspd.utils;

public class Csv
{
    public static String escape( String s )
    {
        if ( s.contains( QUOTE ))
            s = s.replace( QUOTE, ESCAPED_QUOTE );

        if ( s.indexOf( CHARACTERS_THAT_MUST_BE_QUOTED[0] ) > -1
        		|| s.indexOf( CHARACTERS_THAT_MUST_BE_QUOTED[1] ) > -1
        		|| s.indexOf( CHARACTERS_THAT_MUST_BE_QUOTED[2] ) > -1
        		)
            s = QUOTE + s + QUOTE;

        return s;
    }

    public static String unescape( String s )
    {
        if ( s.startsWith( QUOTE ) && s.endsWith( QUOTE ) )
        {
            s = s.substring( 1, s.length() - 2 );

            if ( s.contains( ESCAPED_QUOTE ) )
                s = s.replace( ESCAPED_QUOTE, QUOTE );
        }

        return s;
    }


    private static final String QUOTE = "\"";
    private static final String ESCAPED_QUOTE = "\"\"";
    private static final char[] CHARACTERS_THAT_MUST_BE_QUOTED = { ',', '"', '\n' };
}
