package pruebas;

import java.io.File;

public class Prueba {
     public void walk( String path ) {

        File root = new File( path );
        File[] list = root.listFiles();

        if (list == null) return;

        for ( File f : list ) {
            if ( f.isDirectory() ) {
                walk( f.getAbsolutePath() ); //recursividad
                System.out.println( "Dir:" + f.getAbsoluteFile() );
            }
            else {
                System.out.println( "File:" + f.getAbsoluteFile() );
            }
        }
    }

    public static void main(String[] args) {
        Prueba fw = new Prueba();
        fw.walk("C:\\Users\\kevin\\Desktop\\6TOSEMESTRE\\Redes\\Sockets\\Papu");
    }
    
}
