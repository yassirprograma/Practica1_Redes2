/*
*   ELABORADO POR: KEVIN YASSIR FUENTES GARCÍA, ERICK EDMUNDO GUERRERO ZORZA
*   APLICACIONES PARA COMUNICACIONES EN RED, SEPTIEMBRE 2022
*   ISC ESCOM IPN
*/

package Servidor;

import static Cliente.backendCliente.eliminarDirectorioLocal;
import java.net.*;
import java.io.*;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class ServidorSA {
   
    public static void main(String[] args){

        //Definición de la carpeta raíz del servidor
        final String rutaCarpetaServerRaiz="."+System.getProperty("file.separator")+"archivosServidor"+System.getProperty("file.separator"); //Ruta (constant)de la carpeta del servidor
        
        File directorioServer=new File(rutaCarpetaServerRaiz);        
        directorioServer.mkdir();//SI NO EXISTE LA CARPETA DEL SERVIDOR, LA CREAMOS                                 
        System.out.println("La carpeta del servidor está lista para usarse");
                
        
        try{
            //PUERTO DEL SOCKET DEL SERVIDOR
            int pto =8000;                                                           
            
            //CREACIÓN DEL SOCKET DEL SERVIDOR
            ServerSocket servidor = new ServerSocket(pto);
            System.out.println("Servidor iniciado en el puerto "+pto);                                                          
            
            //Ciclo infinito en espera de nuevos clientes////////////////////////////////////////////////////       
            while(true){ 
                //ESTE CICLO ESPERA A UN CLIENTE, CUANDO EL CLIENTE SE CIERRA, SE QUEDA EN ESPERA DE OTRO (INFINITAMENTE)
                
                System.out.println("Esperando nuevo cliente...");
                
                //Esperamos a que un cliente solicite conexión al servidor 
                Socket cliente = servidor.accept(); //
                DataOutputStream dos = new DataOutputStream(cliente.getOutputStream());
                DataInputStream dis = new DataInputStream(cliente.getInputStream());
                
                //Imprimimos que se ha aceptado la conexión
                System.out.println("Cliente conectado desde "+cliente.getInetAddress()+":"+cliente.getPort()); 
                
                //Establecemos una ruta de navegación para el cliente
                String rutaNavActualCliente=rutaCarpetaServerRaiz;
                
                
                //Enviamos la ruta ruta de navegación al cliente
                envia_path_remoto(dos, rutaNavActualCliente);
                        
                ///Enviamos la lista de nombres de archivos de la carpeta del servidor
                envia_ls_remoto(dos, rutaNavActualCliente);
                                                
                int peticion;//para identificar la petición
                /*                    
                    0:conexión iniciada
                    1: Abrir carpeta hija
                    2: Subir archivos/carpetas
                    3: Descargar archivos/carpetas
                    4. Eliminar archivos /carpetas remotas
                    5. Volver a la carpeta padre
                    6. Salir (cerrar conexión cliente-servidor)
                    7. Crear carpeta vacia en remoto (nueva)
                */
                
                //CICLO PARA MANTENER CONEXIÓN CON EL CLIENTE QUE SE HA CONECTADO////////////////////////                                                            
                label:
                while(true){
                    //CUANDO EL CLIENTE ELIGE CERRAR LA CONEXIÓN (O CIERRA LA VENTANA), ESTE CICLO SE ROMPE
                    
                    //Leemos la petición que envia el cliente
                    peticion=recibePeticion(dis); //se queda esperando a recibir la petición por parte del cliente

                    switch (peticion) {
                        case 0:

                            break;
                        case 1:  //Listar contenido de una carpeta abierta
                            System.out.println("Cliente ha solicitado abrir una carpeta");

                            //recibimos la dirección de la carpeta que se quiere listar
                            rutaNavActualCliente = recibePath(dis); //actualizamos la ruta en la que se encuentra el cliente
                            System.out.println(rutaNavActualCliente);
                            //le mandamos la lista de archivos al abrir esa carpeta
                            envia_ls_remoto(dos, rutaNavActualCliente);
                            break;
                        case 2:
                            System.out.println("Cliente ha solicitado subir un archivo/carpeta");

                            //RECIBIMOS EL ARCHIVO EN LA CARPETA ACTUAL
                            obtenerArchivo(dis, rutaNavActualCliente);

                            //LE MANDAMOS LA ACTUALIZACIÓN DEL LISTADO DE LA CARPETA ACTUAL
                            envia_ls_remoto(dos, rutaNavActualCliente);

                            break;
                        case 3: {
                            System.out.println("Cliente ha solicitado descargar un archivo/carpeta");


                            //recibimos la dirección del archivo que el cliente desea
                            String direccionArchivo = recibePath(dis);
                            File archivoSolicitado = new File(direccionArchivo); //Abrimos el archivo deseado


                            //Le mandamos al cliente el archivo que solicitó
                            enviaArchivo(dos, archivoSolicitado);


                            break;
                        }
                        case 4: {
                            System.out.println("Cliente ha solicitado eliminar un archivo/carpeta");

                            //Recibimos el path del elemento que se desea borrar
                            String direccionArchivo = recibePath(dis);

                            //ELIMINAMOS EL ARCHIVO
                            eliminarArchivo(direccionArchivo);

                            //ACTUALIZAMOS Y MANDAMOS LA LISTA DE ARCHIVOS
                            envia_ls_remoto(dos, rutaNavActualCliente);

                            break;
                        }
                        case 5:

                            //si la actual es igual a la original, entonces no puede volver hacia atrás
                            System.out.println("Cliente ha solicitado volver a la carpeta de atrás (padre)");

                            //Verificamos que no supere a la ruta inicial (rutaCarpetaServer)
                            if (rutaNavActualCliente.equals(rutaCarpetaServerRaiz)) {// si es igual a la inicial, no puede subir de nivel
                                //la mantenemos en el mismo
                                rutaNavActualCliente = rutaCarpetaServerRaiz;
                            } else {
                                //Si la ruta a la que se quiere volver es de menor nivel que la raíz, entonces sí podemos subir de nivel
                                //Obtenemos el padre y actualizamos la ruta actual a esa carpeta
                                rutaNavActualCliente = new File(rutaNavActualCliente).getParent() + System.getProperty("file.separator"); //actualizamos la ruta en la que se encuentra el cliente

                            }


                            //Enviamos el nuevo path (el del padre)
                            envia_path_remoto(dos, rutaNavActualCliente);
                            System.out.println("ruta actual: " + rutaNavActualCliente);


                            //Enviamos el listado
                            envia_ls_remoto(dos, rutaNavActualCliente);


                            break;
                        case 6:

                            //cuando el cliente cierra la ventana, el servidor recibe la petición 6
                            System.out.println("Cliente ha solicitado cerrar la conexión");
                            dis.close();
                            dos.close();
                            cliente.close();
                            System.out.println("Conexión finalizada ");
                            break label;//para romper el ciclo y dejar de atender al cliente actual

                        case 7:  //Peticion agregada en la nueva actualizacion

                            System.out.println("Cliente ha solicitado crear una carpeta");
                            //Cuando el cliente desea subir una carpeta, entonces debe crearse dicha carpeta en el servidor
                            //(Se crea vacía)
                            String nombre = recibePath(dis);
                            System.out.println(nombre);
                            File carpeta = new File(nombre);
                            //sin la diagonal (separador)

                            if (carpeta.mkdir())
                                System.out.println("Directorio creado en local");
                            else
                                System.out.println("Error !");
                            envia_ls_remoto(dos, rutaNavActualCliente);
                            //Recibimos el nombre de la carpeta
                            //Creamos la carpeta:
                            break;
                    }

                }
                //TERMINA CONEXIÓN CON DICHO CLIENTE /////////////////////////////////////////////////////////////////////
                
                cliente.close();//cerrarmos el socket que el cliente ha usado
                
                //Una vez que el cliente ha cerrado conexión, pasa a esperar a que se conecte otro
                System.out.println("El cliente: "+ cliente.getInetAddress()+ " ha abandonado la conexión\n\n\n\n");
                
                
                
            }//Ciclo espera clientes
            /////////////////////////////////////////////////////////////////////////////////////////
            
        }catch(Exception e){
            e.printStackTrace();
        }
       
    }
    
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    ////////////////FUNCIÓN PARA RECIBIR UN NÚMERO QUE IDENTIFICA A LA PETICIÓN DESEADA
    public static int recibePeticion(DataInputStream dis) throws IOException{ //recibe la petición que el cliente envía a través del socket
        int peticion;
        peticion=dis.readInt();   

        return peticion;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    ////FUNCIÓN PARA RECIBIR DEL CLIENTE UNA DIRECCIÓN DE UN ARCHIVO
    public static String recibePath(DataInputStream dis) throws IOException{ //recibe del cliente el path del archivo/carpeta que se desea manipular
        String pathTemp;
        pathTemp= dis.readUTF();//se lee del buffer
        
        return pathTemp;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
            
    
    
    ////////////////FUNCIÓN PARA ENVIAR AL CLIENTE EL PATH DE UNA CARPETA ALOJADA EN EL SERVIDOR////////////////////////////////////// 
    public static void envia_path_remoto(DataOutputStream dos, String path) throws IOException{ //envia el path de la carpeta que se va a listar
         dos.writeUTF(path);
         dos.flush();
         
     }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////FUNCIÓN PARA ENVIAR AL CLIENTE LA LISTA DE ARCHIVOS QUE SE ENCUENTRAN  EN DETERMINADA CARPETA ALOJADA EN EL SERVIDOR////////////////////////////////////// 
    public static void envia_ls_remoto(DataOutputStream dos,String path) throws IOException{ //ENVIA LS REMOTO, DADO UN PATH DENTRO DEL SERVER
        File localFiles = new File(path);
        File[] listaArchivos = localFiles.listFiles();
        String nombre;
        boolean esDir;
        
        int length = listaArchivos.length;
        dos.writeInt(length);
        dos.flush();

        for(File file: listaArchivos){

            esDir = file.isDirectory();
            nombre=file.getName();

            dos.writeBoolean(esDir);
            dos.flush();
            dos.writeUTF(nombre);
            dos.flush();
        }//for        
        
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    //Función para enviar archivo al cliente////////////////////////////////////////////////////////////////////
    public static void enviaArchivo(DataOutputStream dos, File file) throws IOException {
        long file_length = file.length();
        String nombre_archivo = file.getName();
        String path = file.getPath();

        System.out.println("Preparandose pare enviar archivo "+path+"\n\n");

        DataInputStream dis = new DataInputStream(new FileInputStream(path));
        dos.writeUTF(nombre_archivo);//Se envía el nombre del archivo
        dos.flush();
        dos.writeLong(file_length);//Se utiliza la longitud del archivo
        dos.flush();

        long enviados = 0;
        int l,porcentaje;
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
        dis.close();
        
    }
    /////////////////////////////////////////////////////////////////////////////////////////
    
    ////Función para recibir archivo (obtener archivo que el cliente envía)////////////////////////////////////////////////////////////    
    public static void obtenerArchivo(DataInputStream dis, String ruta_archivo) throws IOException {
        //ruta_archivo es la carpeta del servidor en donde se guardará el archivo que ha enviado el cliente
        String nombre = dis.readUTF();
        long tam = dis.readLong();//Se obtiene el tamaño
        System.out.println("Comienza descarga del archivo "+nombre+" de "+tam+" bytes\n\n");
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(ruta_archivo+nombre));
        long recibidos=0;//
        int l, porcentaje;
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
        dos.close();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    ///Función para eliminar un directorio //////////////
    public static void eliminarDirectorio(File file){  
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
