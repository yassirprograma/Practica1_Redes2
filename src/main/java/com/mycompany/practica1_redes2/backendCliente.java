package com.mycompany.practica1_redes2;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;

public class backendCliente {
    
    
    public static Socket creaSocket() throws UnsupportedEncodingException, IOException{ //para crear un socket y conectarse al servidor
        int pto = 8000;
            BufferedReader entradaTeclado = new BufferedReader(new InputStreamReader(System.in,"ISO-8859-1"));//CONJUNTO DE CARACTERES QUE ACEPTA EL ESPAÑOL LATINO (buffer de teclado)
            InetAddress host = null; //PARA GUARDAR LA DIRECCIÓN 1P
            String dir=""; //CADENA PARA LEER LA IP
            
            try{                
                System.out.println("Escribe la direccion del servidor :"); 
                dir = entradaTeclado.readLine();  //Leemos COMO CADENA la dirección del server (DESDE TECLADO)
                host = InetAddress.getByName(dir); //Pasa de CADENA al formato de dirección IP
            }catch(Exception n){
                System.out.println("Error al encontrar la IP, vuelva a ingresar la IP");                                                
            }

            Socket temp = new Socket(host,pto); //Establecemos conexión con el servidor mediante el socket (INSTANCIAMOS EL SOCKET CLIENTE)            
            System.out.println("Conexion establecida con el servidor \n"); //IMPRIMIMOS QUE SE HA CONECTADO CON EL SERVER
        
        return temp;
    }
    
     public static void enviaPeticion(Socket socket, int i) throws IOException{ 
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
         dos.writeInt(i); 
         dos.flush();
         
    }
    
     public static void enviaPath(Socket socket, String path) throws IOException{ //envía al server el path de la carpeta/archivo que el cliente ha seleccionado desde la interfaz
         DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
         dos.writeUTF(path);
         dos.flush();
         
     }
    
    public static String obtener_path_remoto(Socket socket) throws IOException{
        String temp=new String();
        DataInputStream dis = new DataInputStream(socket.getInputStream());        
        temp=dis.readUTF();
        
        return temp;
    }

    
    public static String[] obtener_ls_remoto(Socket socket) throws IOException { 
        
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        int numArchivos = dis.readInt();
        String[] temp = new String[numArchivos];    
        
        
        boolean[] type = new boolean[numArchivos];
        for(int i=0;i<numArchivos;i++){
            
            if(dis.readBoolean()==true){ //si es carpeta, agregamos una diagonal al final
                temp[i]=dis.readUTF()+System.getProperty("file.separator");
                System.out.println(temp[i]);
            }else{//si es archivo, se manda tal cual
                temp[i]= dis.readUTF();
                System.out.println(temp[i]);
            }
        }
        
        
        return temp;
    }

    public static void navegarLocal(String dir){ 
        File temp = new File(dir);
        if(temp.exists()){
            if(temp.isDirectory()){
                //rutaActual=rutaActual+fileSeparator+dir;
            }
        }else{
            System.out.println("No se encontraron coincidencias");
        }
    }
    
    ////Función para obtener archivos////////////////////////////////////////////////////////////    
    public static void obtenerArchivos(Socket socket, String ruta_archivos) throws IOException { 
        
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        String nombre = dis.readUTF();
        
        long tam = dis.readLong();//Se obtiene el tamaño
        
        System.out.println("Comienza descarga del archivo "+nombre+" de "+tam+" bytes\n\n");
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(ruta_archivos+nombre));
        long recibidos=0;//
        int l=0, porcentaje=0;
        while(recibidos<tam){
            byte[] b = new byte[1500];
            l = dis.read(b);
            System.out.println("leidos: "+l);
            dos.write(b,0,l);
            dos.flush();
            recibidos = recibidos + l;
            porcentaje = (int)((recibidos*100)/tam);            
            System.out.println("\rRecibido el "+ porcentaje +" % del archivo");            
        }//while
        System.out.println("\nTransmisión finalizada \n");
        
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    //Función para enviar archivos ////
    public static void enviaArchivos(Socket socket, File file) throws IOException {
        long file_length = file.length();
        String nombre_archivo = file.getName();
        String path = file.getAbsolutePath();

        System.out.println("Preparandose pare enviar archivo "+path+"\n\n");

        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        DataInputStream dis = new DataInputStream(new FileInputStream(path));
        dos.writeUTF(nombre_archivo);//Se envía el nombre del archivo
        dos.flush();
        dos.writeLong(file_length);//Se utiliza la longitud del archivo
        dos.flush();

        long enviados = 0;
        int l=0,porcentaje=0;
        while(enviados<file_length){//Se utiliza la longitud del archivo
            byte[] b = new byte[1500];
            l=dis.read(b);
            System.out.println("enviados: "+l);
            dos.write(b,0,l);// dos.write(b);
            dos.flush();
            enviados = enviados + l;
            porcentaje = (int)((enviados*100)/file_length);//Se utiliza la longitud del archivo
            System.out.print("\rEnviado el "+porcentaje+" % del archivo");

        }//while

        System.out.println("\nArchivo enviado..");

        
    }
    /////////////////////////////////////////////////////////////////////////////////////////

    
    ///Función para eliminar un directorio //////////////
    public static void eliminarDirectorioLocal(File file){  
        File[] contenidoDir = file.listFiles();
        if(contenidoDir != null){
            for(File child : contenidoDir){
                child.delete();//Se eliminan los posibles hijos
            }
        }
        file.delete(); // Se Eliminan el directorio padre
    }
    
    //Función para eliminar un archivo//////////////////////////////////////////////////////
    public static void eliminarArchivoLocal(String nombre){
        //RECIBE EL NOMBRE (PATH) DE UN ARCHIVO
        
        File temp = new File(nombre);
        if(temp.isDirectory()){
            eliminarDirectorioLocal(temp);
        }else{
            if(temp.delete()){
                System.out.println("Elemento eliminado\n");
            }else{
                System.out.println("No se pudo eliminar elemento \n");
            }//if
        }//if
    }
       
}
