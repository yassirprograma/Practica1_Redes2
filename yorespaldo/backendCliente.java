/*
*   ELABORADO POR: KEVIN YASSIR FUENTES GARCÍA, ERICK EDMUNDO GUERRERO ZORZA
*   APLICACIONES PARA COMUNICACIONES EN RED, SEPTIEMBRE 2022
*   ISC ESCOM IPN
*/

package Cliente;

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
import java.util.ArrayList;
import java.util.Arrays;

public class backendCliente {
    
    
    //para crear un socket y conectarse al servidor
    public static Socket creaSocket() throws UnsupportedEncodingException, IOException{ 
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
    ///////////////////////////////////////////////////////////////////////////////////////
    
    //////////////////////Función para enviar un entero que indica la petición que el servidor debe atender
     public static void enviaPeticion(Socket socket, int i) throws IOException{ 
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
         dos.writeInt(i); 
         dos.flush();
         
    }
    ///////////////////////////////////////////////////////////////////////////////////////
     
     //////////////////////Función para enviar al servidor la dirección de determinado elemento (archivo o carpeta)     
     public static void enviaPath(Socket socket, String path) throws IOException{ //envía al server el path de la carpeta/archivo que el cliente ha seleccionado desde la interfaz
         DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
         dos.writeUTF(path);
         dos.flush();
         
     }
    ///////////////////////////////////////////////////////////////////////////////////////

    //////////////////////Función para recibir desde el servidor la dirección referente a un archivo o carpete       
    public static String obtener_path_remoto(Socket socket) throws IOException{
        String temp=new String();
        DataInputStream dis = new DataInputStream(socket.getInputStream());        
        temp=dis.readUTF();
        
        return temp;
    }
    ///////////////////////////////////////////////////////////////////////////////////////
    
    
    //////////////////////Función para obtener la lista de los nombres de archivos que se encuentran en alguna carpeta del servidor
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
    ///////////////////////////////////////////////////////////////////////////////////////

    
    ////Función para obtener múltiples archivos/////////////////////////////////////////////////////////////
    //Es recursiva, entonces puede descargar carpetas con otras carpetas o archivos dentro (puede descargar todo un arbol de archivos completo)
    public static void obtenerMultiplesArchivos(Socket socket, String listaArchivos[], String rutaActualArchivosRemotos, String rutaLocalGuardado ) throws IOException{
        //Esta función recibe el socket, la lista de archivos seleccionados, la ruta padre de los archivos seleccionados, y la ruta donde queremos que se guarden los archivos obtenidos
        
        for(int i=0;i<listaArchivos.length;i++){
            //POR CADA ARCHIVO/carpeta                        

            //VERIFICAMOS SI ES CARPETA O ARCHIVO
            if(listaArchivos[i].endsWith(System.getProperty("file.separator"))){ 
                //Si termina con / , entonces es carpeta y se debe hacer lo siguiente:                
                                      
                //SOLICITAMOS ABRIR ESA CARPETA REMOTA                                
                //Nombre de la carpeta remota a descargar:
                String nameCarpetaADescargar=listaArchivos[i];
                
                //Generamos la dirección, según el nombre de la carpeta seleccionada y el path que conocemos 
                String rutaCarpeta=rutaActualArchivosRemotos+nameCarpetaADescargar;                                                       
                              
                //Le mandamos la petición al cliente que queremos listar una carpeta hija                
                backendCliente.enviaPeticion(socket,1); //petición 1= abrir carpeta hija
                                
                //Le mandamos la dirección de la carpeta que deseamos abrir/listar                                                                               
                backendCliente.enviaPath(socket, rutaCarpeta);
                                
                //Recibimos la lista de archivos  de la carpeta que queremos descargar
                String nameArchivosHijosCarpeta[];                                                       
                nameArchivosHijosCarpeta = obtener_ls_remoto(socket);                
                
                
                //Creamos un directorio con el mismo nombre en local
                String pathDirLocal=rutaLocalGuardado+nameCarpetaADescargar;
                
                File carpeta = new File(pathDirLocal.substring(0, pathDirLocal.length()-1)); //sin la diagonal (separador)
                    boolean D1 = carpeta.mkdir();  
                    if(D1)
                       System.out.println("Directorio "+pathDirLocal.substring(0, pathDirLocal.length()-1)+" creado en local");  
                    else  
                       System.out.println("Error !");                  
                
                
                //Mandamos a descargar cada uno de los archivos de esa carpeta:
                //Llamando recursivamente a esta misma función:
                obtenerMultiplesArchivos(socket,
                                        nameArchivosHijosCarpeta,
                                        rutaCarpeta,
                                        pathDirLocal
                                        );
                
                //Salimos de la carpeta que estabamos descargando
                //En remoto
                    //ENVIAMOS LA PETICION
                    enviaPeticion(socket,5); //petición 5= volver atrás (carpeta padre)                        
                        
                    //Recibimos el nuevo path (el path del padre)
                    obtener_path_remoto(socket);
                        
                    //OBTENEMOS LA LISTA DE LA NUEVA CARPETA
                    obtener_ls_remoto(socket);
                
                
            }else {
                //Si no, entonces es archivo y debemos hacer lo siguiente:                
                
                //ENVIAMOS LA PETICIÓN AL SERVIDOR
                enviaPeticion(socket,3); //petición 3= descargar archivo  
                
                //Nombre del elemento remoto a descargar
                String nameElementoADescargar=listaArchivos[i];
                
                //ENVIAMOS AL SERVER EL PATH DEL ARCHIVO QUE DESEAMOS  DESCARGAR                                
                enviaPath(socket,rutaActualArchivosRemotos+nameElementoADescargar);
                
                //SE SINCRONIZA CON EL SERVER PARA RECIBIR  EL ARCHIVO
                obtenerArchivo(socket,rutaLocalGuardado);//lo vamos a guardar en la carpeta que se nos indicó 
            }                         
        }                
    }        
    ///////////////////////////////////////////////////////////////////////////////////////
    

    ////Función para obtener archivo del servidor////////////////////////////////////////////////////////////    
    public static void obtenerArchivo(Socket socket, String ruta_archivo) throws IOException {  
    //la ruta_archivo es de la carpeta en donde se guardará el archivo obtenido/descargado
        
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        String nombre = dis.readUTF();
        
        long tam = dis.readLong();//Se obtiene el tamaño
        
        System.out.println("Comienza descarga del archivo "+nombre+" de "+tam+" bytes\n\n");
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(ruta_archivo+nombre));
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
    
    //Función para enviar archivo al servidor a través de un socket//////////////////////////////////////////
    public static void enviaArchivo(Socket socket, File file) throws IOException {
        ///file es el archivo que se va a enviar
        
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

    
    ///Función para eliminar un directorio local//////////////
    public static void eliminarDirectorioLocal(File file){  
        File[] contenidoDir = file.listFiles();
        if(contenidoDir != null){
            for(File child : contenidoDir){                
               if(child.isDirectory()){ //si el hijo es directorio, lo eliminamos recursivamente
                   eliminarDirectorioLocal(child);                   
               }
               child.delete();                              
            }
        }
        file.delete(); // Se Elimina el directorio padre
    }
    /////////////////////////////////////////////////////////////////////////////////////////
    
    //Función para eliminar un archivo local//////////////////////////////////////////////////////
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
    /////////////////////////////////////////////////////////////////////////////////////////
}
