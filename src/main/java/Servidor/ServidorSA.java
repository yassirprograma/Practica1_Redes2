package Servidor;

import java.net.*;
import java.io.*;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class ServidorSA {
   
    public static void main(String[] args) throws IOException{

        
        final String rutaCarpetaServerRaiz="."+System.getProperty("file.separator")+"archivosServidor"+System.getProperty("file.separator"); //Ruta (constant)de la carpeta del servidor
                       
        File directorioServer=new File(rutaCarpetaServerRaiz);
        
        directorioServer.mkdir();//SI NO EXISTE LA CARPETA DEL SERVIDOR, LO CREAMOS                         
        
        System.out.println("La carpeta del servidor está lista para usarse");
        

        
        
        try{
            int pto =8000;                                                           
            
            ServerSocket servidor = new ServerSocket(pto);
            System.out.println("Servidor iniciado en el puerto "+pto);                                                          
            
            while(true){ //Este ciclo espera a los clientes       
                
                System.out.println("Esperando nuevo cliente...");
                
                //Esperamos a que un cliente solicite conexión al servidor 
                Socket cliente = servidor.accept(); //
                
                //Imprimimos que se ha aceptado la conexión
                System.out.println("Cliente conectado desde "+cliente.getInetAddress()+":"+cliente.getPort()); 
                
                //Establecemos una ruta 
                String rutaNavActualCliente=rutaCarpetaServerRaiz;
                
                
                //enviamos la ruta de la carpeta 
                envia_path_remoto(cliente, rutaNavActualCliente);
                        
                ///enviamos la lista de nombres de archivo de la carpeta del servidor
                envia_ls_remoto(cliente, rutaNavActualCliente);
                                                
                int peticion=0;//para identificar la petición
                /*                    
                    0:conexión iniciada
                    1: Abrir carpeta hija
                    2: Subir archivos/carpetas
                    3: Descargar archivos/carpetas
                    4. Eliminar  archivos /carpetas remotas
                    5. Volver a la carpeta padre
                    6. Salir (cerrar conexión cliente-servidor)
                */
                while(true){//CICLO PARA MANTENER CONEXIÓN CON EL CLIENTE QUE SE HA CONECTADO                                        
                    
                    //Leemos la petición
                    peticion=recibePeticion(cliente); //se queda esperando a recibir la petición por parte del cliente                    
                    
                    if(peticion==0){ 
                        
                    }else if(peticion==1){ //Listar contenido de una carpeta abierta
                        System.out.println("Cliente ha solicitado abrir una carpeta");
                        
                        //recibimos la dirección de la carpeta que se quiere listar                           
                        rutaNavActualCliente=recibePath(cliente); //actualizamos la ruta en la que se encuentra el cliente
                        
                        System.out.println(rutaNavActualCliente);
                        
                        //le mandamos la lista de archivos al abrir esa carpeta
                        envia_ls_remoto(cliente,rutaNavActualCliente);                                                                                                
                        
                        
                    } else if(peticion==2){
                        System.out.println("Cliente ha solicitado subir un archivo/carpeta");
                        
                        //RECIBIMOS EL ARCHIVO EN LA CARPETA ACTUAL                        
                        obtenerArchivos(cliente,rutaNavActualCliente);
                        
                        //LE MANDAMOS LA ACTUALIZACIÓN DEL LISTADO DE LA CARPETA ACTUAL
                        envia_ls_remoto(cliente, rutaNavActualCliente);
                        
                    } else if(peticion==3){                        
                        System.out.println("Cliente ha solicitado descargar un archivo/carpeta");  
                        
                        //recibimos la dirección del archivo que el cliente desea
                        String direccionArchivo=recibePath(cliente); 
                        File archivoSolicitado = new File(direccionArchivo); //Abrimos el archivo deseado
                        
                        //Le mandamos al cliente el archivo que solicitó
                        enviaArchivos(cliente,archivoSolicitado); 
                                             
                        
                        
                    } else if(peticion==4){
                        System.out.println("Cliente ha solicitado eliminar un archivo/carpeta");                        
                        
                        //Recibimos el path del elemento que se desea borrar
                        String direccionArchivo=recibePath(cliente); 
                        
                        //ELIMINAMOS EL ARCHIVO
                        eliminarArchivo(direccionArchivo);
                        
                        //ACTUALIZAMOS Y MANDAMOS LA LISTA DE ARCHIVOS
                        envia_ls_remoto(cliente, rutaNavActualCliente);
                        
                    }else if(peticion==5){
                        
                        //si la actual es igual a la original, entonces no puede volver hacia atrás                        
                        System.out.println("Cliente ha solicitado volver a la carpeta de atrás (padre)");                        
                                                
                        //Verificamos que no supere a la ruta inicial (rutaCarpetaServer)
                        if(rutaNavActualCliente.equals(rutaCarpetaServerRaiz)){// si es igual a la inicial, no puede subir de nivel
                            //la mantenemos en el mismo
                            rutaNavActualCliente=rutaCarpetaServerRaiz;
                        }else{
                            //Si la ruta a la que se quiere volver es de menor nivel que la raíz, entonces sí podemos subir de nivel                            
                            //Obtenemos el padre y actualizamos la ruta actual a esa carpeta                                                                     
                            rutaNavActualCliente=new File(rutaNavActualCliente).getParent()+System.getProperty("file.separator"); //actualizamos la ruta en la que se encuentra el cliente                                                
                        }
                        
                        
                        //Enviamos el nuevo path (el del padre)
                        envia_path_remoto(cliente,rutaNavActualCliente);
                        System.out.println("ruta actual: "+rutaNavActualCliente);
                        
                        
                        //Enviamos el listado
                        envia_ls_remoto(cliente,rutaNavActualCliente);
                        
                        
                    } else if(peticion==6){ 
                        
                        //cuando el cliente cierra la ventana, el servidor recibe la petición 6
                        System.out.println("Cliente ha solicitado cerrar la conexión");
                        break;//para romper el ciclo y dejar de atender al cliente actual
                    }
                    
                }                                
                
                cliente.close();//cerrarmos el socket que el cliente ha usado
                
                //Una vez que el cliente ha cerrado, pasa a esperar a que se conecte otro
                System.out.println("El cliente: "+ cliente.getInetAddress()+ " ha abandonado la conexión\n\n\n\n");
                
                
            }
        }catch(Exception e){
            e.printStackTrace();
        }
       
    }
    
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public static int recibePeticion(Socket socket) throws IOException{ //recibe la petición que el cliente envía a través del socket
        int peticion;
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        peticion=dis.readInt();   
        
        
        return peticion;
    }
    
    public static String recibePath(Socket socket) throws IOException{ //recibe del cliente el path del archivo/carpeta que se desea manipular
        String pathTemp;
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        pathTemp= dis.readUTF();//se lee del buffer
        
        
        return pathTemp;
    }
    
    
     public static void eliminarArchivo(Socket socket, String rutaCarpeta)throws IOException{
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        String nombre_archivo = dis.readUTF();
        File temp = new File(rutaCarpeta+nombre_archivo);
        if(temp.isDirectory()){
            eliminarDirectorio(temp);
        }else{
            temp.delete();
            System.out.println("Elemento remoto eliminado\n");
        }
        
    }
    
     public static void envia_path_remoto(Socket socket, String path) throws IOException{ //envia el path de la carpeta que se va a listar
         DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
         dos.writeUTF(path);
         dos.flush();
         
     }
    
    public static void envia_ls_remoto(Socket socket,String path) throws IOException{ //ENVIA LS REMOTO, DADO UN PATH DENTRO DEL SERVER
        
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        File localFiles = new File(path);
        File[] listaArchivos = localFiles.listFiles();
        String nombre;
        boolean esDir;
        
        int length = listaArchivos.length;
        dos.writeInt(length);
        dos.flush();

        for(File file: listaArchivos){
            esDir = false;
            if(file.isDirectory()){
                esDir = true;
            }
            nombre=file.getName();

            dos.writeBoolean(esDir);
            dos.flush();
            dos.writeUTF(nombre);
            dos.flush();
        }//for
        
        
    }
    
     //Función para enviar archivos ////
    public static void enviaArchivos(Socket socket, File file) throws IOException {
        long file_length = file.length();
        String nombre_archivo = file.getName();
        String path = file.getPath();

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
            System.out.println("\rEnviado el "+porcentaje+" % del archivo");

        }//while

        System.out.println("\nArchivo enviado..");

        
    }
    /////////////////////////////////////////////////////////////////////////////////////////
    
     ////Función para recibir archivos////////////////////////////////////////////////////////////    
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
            System.out.print("\rRecibido el "+ porcentaje +" % del archivo");
        }//while
        
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    ///Función para eliminar un directorio //////////////
    public static void eliminarDirectorio(File file){  
        File[] contenidoDir = file.listFiles();
        if(contenidoDir != null){
            for(File child : contenidoDir){
                child.delete();//Se eliminan los posibles hijos
            }
        }
        file.delete(); // Se Eliminan el directorio padre
    }
    
    //Función para eliminar un archivo//////////////////////////////////////////////////////
    public static void eliminarArchivo(String nombre){
        //RECIBE EL NOMBRE (PATH) DE UN ARCHIVO
        
        File temp = new File(nombre);
        if(temp.isDirectory()){
            eliminarDirectorio(temp);
        }else{
            if(temp.delete()){
                System.out.println("Elemento eliminado\n");
            }else{
                System.out.println("No se pudo eliminar elemento \n");
            }//if
        }//if
    }
    ////////////////////////////////////////////////////////////////////////////////////////
    
}
