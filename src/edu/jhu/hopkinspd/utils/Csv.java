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
