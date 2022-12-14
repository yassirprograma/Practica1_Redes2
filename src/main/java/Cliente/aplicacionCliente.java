/*
*   ELABORADO POR: KEVIN YASSIR FUENTES GARCÍA, ERICK EDMUNDO GUERRERO ZORZA
*   APLICACIONES PARA COMUNICACIONES EN RED, SEPTIEMBRE 2022
*   ISC ESCOM IPN
*/

package Cliente;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class aplicacionCliente extends JFrame { //Hereda métodos existentes en la clase JFrame       
    
    //ATRIBUTOS PARA GUARDAR INFORMACIÓN DE LA INTERACCIÓN DEL CLIENTE 
       String nombreElementoRemotoSeleccionado; //PARA GUARDAR EL NOMBRE DEL ARCHIVO REMOTO SELECCIONADO POR EL USUARIO           
       String  listaArchivosRemotosSeleccionados[]; //PARA GUARDAR CUANDO LA SELECCIÓN DE ARCHIVOS REMOTOS ES MÚLTIPLE        
       
       File  archivosLocalesSeleccionados[]; //Para guardar los archivos seleccionados en el JFILECHOOSER
        
               

    //ruta local a donde se van a ir los archivos descargados
        final String rutaDescargas="."+System.getProperty("file.separator")+"archivosDescargados"+System.getProperty("file.separator"); 
        
        String rutaActualArchivos; //la ruta de la carpeta remota que se muestra en la pantalla del cliente
        String listaArchivos []; //la lista que se despliega en pantalla
        
        
    /////////////////////ATRIBUTOS/ELEMENTOS DE LA INTERFAZ GRÁFICA////////////////////////////////////////////////////////////////////////
        
        //La indentación indica qué cosa está dentro de otra            
        Font fontTitulos; //fuentes

    JPanel divLadoServer; //contenedor para lo relacionado a la manipulación de la carpeta del cliente
            JPanel divCarpetaRemota;    
                JLabel tituloRemota; //para indicar que es la carpeta remota
                JScrollPane divScrollRemota; //Div con scroll para la carpeta remota    
                    JList <String> listaCarpetaRemota; //Para listar los elementos de la carpeta
            JPanel divOpcionesRemota; //div para alojar la barra de opciones        
                JToolBar opcionesCarpetaRemota; //Barra de opciones para la carpeta remota
                    JButton btnAbrirCarpeta; 
                    JButton btnCerrarConexion;
                    JButton btnDescargarArchivo;
                    JButton btnEliminarArchivoRemoto;
                    JButton btnAtrasCarpeta;

        JPanel divLogs; //Aquí mostrará mensajes que indiquen lo que está sucediendo         
            JPanel divLogServidor;
                JLabel tituloLogsServidor;
                JScrollPane logsServidorScroll; //scrolleable 
                    JTextArea logsCarpetaServidor;


            JPanel divLogCliente;
                JLabel tituloLogsCliente;
                JScrollPane logsClienteScroll; //scrolleable
                    JTextArea logsCarpetaCliente;


        JPanel divLadoCliente; //contenedor para lo relacionado a la manipulación de la carpeta del cliente  
            JPanel divCarpetaLocal; //para alojar el file chooser y el título
                JLabel tituloLocal; //para indicar que es la carpeta remota
                JFileChooser navegadorCarpetaLocal; // Navegador de archivos para seleccionar 1 archivo    
            JPanel divOpcionesLocal; //div para alojar la barra de opciones
                JToolBar opcionesCarpetaLocal; //barra de herramientas para la carpeta local (botones)
                    JButton btnSubirArchivo;
                    JButton btnEliminarArchivoLocal;
    
    /////////////////////////////FUNCIÓN MAIN (EJECUCIÓN DE LA APLICACIÓN)///////////////////////////////////////////////////
     public static void main(String[] args) throws UnsupportedEncodingException, IOException{               
                        
        //////////////INSTANCIACIÓN DE LA INTERFAZ GRÁFICA DE LA APLICACIÓN//////////////////////////////////////////
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize(); //tamaño completo               
            dim.width=dim.width*4/6; dim.height=dim.height*55/64; //establecemos dimensiones adecuadas        
            aplicacionCliente aplicacion =new aplicacionCliente(dim); //instanciamos una interfaz (class interfazCliente)    
            //ventana.setExtendedState(JFrame.MAXIMIZED_BOTH);                
            aplicacion.setDefaultCloseOperation(aplicacionCliente.EXIT_ON_CLOSE);                
            aplicacion.setSize(dim);              
            aplicacion.setResizable(false);             
            
            aplicacion.nombreElementoRemotoSeleccionado="*"; //ningún archivo remoto se encuentra seleccionado en la ventana
            aplicacion.navegadorCarpetaLocal.setCurrentDirectory(new File(aplicacion.rutaDescargas));
            aplicacion.btnAbrirCarpeta.setEnabled(false);         
            aplicacion.btnDescargarArchivo.setEnabled(false);
            aplicacion.btnEliminarArchivoRemoto.setEnabled(false);
            aplicacion.btnAtrasCarpeta.setEnabled(true);

            aplicacion.btnSubirArchivo.setEnabled(false);
            aplicacion.btnEliminarArchivoLocal.setEnabled(false);
            
            
        ////////////////////////////////////////////////////////////////////////                                
        
        ////////////// INICIALIZACIÓN DEL SOCKET POR EL QUE APLICACIÓN SE COMUNICA///////////////            
        
            //Declaramos el socket a través del cual habrá comunicación con el servidor//
            Socket socketCliente = backendCliente.creaSocket();
            DataOutputStream dos = new DataOutputStream(socketCliente.getOutputStream());
            DataInputStream dis = new DataInputStream(socketCliente.getInputStream());
                        
            
            ///Solicitamos al servidor la ruta relativa de los archivos del server
            aplicacion.rutaActualArchivos=backendCliente.obtener_path_remoto(dis); //la ruta que maneja la interfaz
            System.out.println(aplicacion.rutaActualArchivos);
         
            //Solicitamos al servidor la lista de nombres de archivos de la carpeta raíz remota
            aplicacion.listaArchivos=backendCliente.obtener_ls_remoto(dis);
            
            //Establecemos o preparamos la carpeta de descargas (si no existe, la creamos)
            File carpetaDescargas=new File(aplicacion.rutaDescargas);        
            carpetaDescargas.mkdir();//SI NO EXISTE LA CARPETA DEL SERVIDOR, LO CREAMOS                         
            
            aplicacion.setVisible(true);    
        /////////////////////////////////////////////////////////////////////////////////////////

        ///Actualizamos la ventana después de la interacción del socket con el servidor/////////////
            aplicacion.listaCarpetaRemota.setListData(aplicacion.listaArchivos); //actualizamos la JList, con la lista de nombres de archivo obtenida del servidor       
            aplicacion.tituloRemota.setText("Carpeta remota:"+aplicacion.rutaActualArchivos);
                                                
        //////////////////////////////////////////////////////////////////////////////////////
        
        
        //////DEFINICIÓN DE LOS EVENTOS DE LOS ELEMENTOS DE LA INTERFAZ/////////////                  
        
            //////////////////////////////////////EVENTOS PARA LA LISTA DE NOMBRES DE ARCHIVO QUE SE MUESTRA/////////////
            aplicacion.listaCarpetaRemota.addMouseListener(new MouseListener(){ //escucha activa
                public void mouseClicked(MouseEvent e){    
                    int idArchivo=aplicacion.listaCarpetaRemota.locationToIndex(e.getPoint());                     
                    
                    aplicacion.nombreElementoRemotoSeleccionado=aplicacion.listaArchivos[idArchivo];                                    
                                                            
                    
                    if(aplicacion.nombreElementoRemotoSeleccionado.endsWith(System.getProperty("file.separator"))){                       
                        
                        //si el nombre termina con separador, entonces es una carpeta                        
                        aplicacion.logsCarpetaServidor.append("Cerpeta seleccionada: "+aplicacion.nombreElementoRemotoSeleccionado+"\n");                                      
                        
                        //Activamos el botón de descargar
                        aplicacion.btnDescargarArchivo.setEnabled(true); //////////////
                        
                        //activamos el botón de abrir carpeta
                        aplicacion.btnAbrirCarpeta.setEnabled(true);   //////////////
                        
                    }else {
                        //si no es carpeta, entonces es archivo                        
                        aplicacion.logsCarpetaServidor.append("Archivo seleccionado: "+aplicacion.nombreElementoRemotoSeleccionado+"\n");                
                        
                        aplicacion.btnDescargarArchivo.setEnabled(true); //////////////
                        //desactivamos el botón de abrir carpeta
                        aplicacion.btnAbrirCarpeta.setEnabled(false);         
                    }
                    
    
                    //Una vez que se ha elegido un archivo, ya se pueden activar los botones para realizar determinada acción
                    
                    aplicacion.btnEliminarArchivoRemoto.setEnabled(true);                                                            
                    
                }
                
                //otros tipos de eventos
                public void mouseEntered(MouseEvent e){} public void mouseExited(MouseEvent e){} public void mousePressed(MouseEvent e){} public void mouseReleased(MouseEvent e){}             
                
            });       
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            
            
            //////////////////////////////////////EVENTOS PARA EL JFILECHOOSER ////////////////////////////////////////////////////
            aplicacion.navegadorCarpetaLocal.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent evento) {
                                         
                   
                   String command = evento.getActionCommand();
                   
                  if (command.equals(JFileChooser.APPROVE_SELECTION)) {
                      
                      
                     //Obtenemos un arreglo con los archivos uque han sido seleeccionados con el JFILECHOOSER
                      aplicacion.archivosLocalesSeleccionados= aplicacion.navegadorCarpetaLocal.getSelectedFiles();
                          
                       //Indicamos la cantidad de elementos seleccionados
                       aplicacion.logsCarpetaCliente.append("Se han seleccionado "+aplicacion.archivosLocalesSeleccionados.length+" archivos\n");
                       System.out.println("Se han seleccionado "+aplicacion.archivosLocalesSeleccionados.length+" archivos\n");                     
                       
                       
                        aplicacion.btnSubirArchivo.setEnabled(true);
                                                                    
                        aplicacion.btnEliminarArchivoLocal.setEnabled(true);         
                                                                                                                                         
                       

                   }  else if (command.equals(JFileChooser.CANCEL_SELECTION)) {                    
                       
                       aplicacion.logsCarpetaCliente.append("Ningún archivo seleccionado\n");
                       System.out.println("Ningún archivo seleccionado");

                       //si no se ha elegido ninguno, (se ha cancelado la selección), entonces volvemos a bloquear los botones 
                       aplicacion.btnSubirArchivo.setEnabled(false);
                       aplicacion.btnEliminarArchivoLocal.setEnabled(false);    

                   }     
                   
               }

            });
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            
            
            
             /*    //DEPENDIENDO DEL EVENTO (clicks sobre los botones), ES LA PETICIÓN QUE REALIZARÁ AL SERVIDOR                    
                    0:conexión iniciada
                    1: Abrir carpeta hija
                    2: Subir archivos/carpetas
                    3: Descargar archivos/carpetas
                    4. Eliminar  archivos /carpetas remotas
                    5. Volver a la carpeta padre
                    6. Salir (cerrar conexión cliente-servidor)
            */                        
            
             
            ///////////////////////////EVENTTOS PARA EL BOTÓN DE ABRIR CARPETA//////////////////////////////////////////////////////////////////////////
            aplicacion.btnAbrirCarpeta.addMouseListener(new MouseListener(){ //escucha activa
                public void mouseClicked(MouseEvent e){    
                    try {                        
                        //le mandamos la petición al cliente que queremos listar una carpeta hija
                        backendCliente.enviaPeticion(dos,1); //petición 1= abrir carpeta hija
                        
                        //generamos la dirección, según el nombre de la carpeta seleccionada y el path que conocemos 
                        aplicacion.rutaActualArchivos=aplicacion.rutaActualArchivos+aplicacion.nombreElementoRemotoSeleccionado;                                       
                        
                                System.out.println(aplicacion.rutaActualArchivos);
                        
                        //le mandamos la dirección de la carpeta que deseamos abrir/listar                                                                               
                        backendCliente.enviaPath(dos, aplicacion.rutaActualArchivos);
                        
                        //recibimos la lista de la carpeta enviada desde el servidor
                        aplicacion.listaArchivos=backendCliente.obtener_ls_remoto(dis);
                        
                        //Actualizamos la jlist
                        aplicacion.listaCarpetaRemota.setListData(aplicacion.listaArchivos);
                        
                        //Actualizamos el log
                        aplicacion.logsCarpetaServidor.append("Carpeta listada: "+ aplicacion.rutaActualArchivos+"\n");
                        
                        //bloqueamos nuevamente el botón de abrir carpeta (por que se acaba de entrar a una nueva carpeta)
                        aplicacion.btnAbrirCarpeta.setEnabled(false);
                        
                    } catch (IOException ex) {
                        Logger.getLogger(aplicacionCliente.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                //otros tipos de eventos
                public void mouseEntered(MouseEvent e){} public void mouseExited(MouseEvent e){} public void mousePressed(MouseEvent e){} public void mouseReleased(MouseEvent e){}             
                
            });     
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            
            
            ///////////////////////////EVENTTOS PARA EL BOTÓN DE SUBIR ARCHIVO//////////////////////////////////////////////////////////////////////////
            aplicacion.btnSubirArchivo.addMouseListener(new MouseListener(){ //escucha activa
                public void mouseClicked(MouseEvent e){
                 
                    
                    try {
                        
                         //Mandamos a llamar a la función que envía/sube multiples archivos
                         backendCliente.enviaMultiplesArchivos(dos,dis, aplicacion.archivosLocalesSeleccionados, aplicacion.rutaActualArchivos);
                         
                                                                                                
                         
                        
                        aplicacion.logsCarpetaCliente.append("Archivos subidos...\n");
                        aplicacion.logsCarpetaServidor.append("Archivos recibidos ...\n");
                        
                        //Actualizamos EL LISTADO DE LA CARPETA ACTUAL (actualizamos la ventana)
                        backendCliente.enviaPeticion(dos, 8); //Peticion para obtener actualizacion de la lista de carpeta remota
                        aplicacion.listaArchivos=backendCliente.obtener_ls_remoto(dis);
                        
                        aplicacion.listaCarpetaRemota.setListData(aplicacion.listaArchivos);                                                
                        
                        
                        
                    } catch (IOException ex) {
                        Logger.getLogger(aplicacionCliente.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }                
                //otros tipos de eventos
                public void mouseEntered(MouseEvent e){} public void mouseExited(MouseEvent e){} public void mousePressed(MouseEvent e){} public void mouseReleased(MouseEvent e){}             
            });     
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            
            
            
            
            
            
            ///////////////////////////EVENTTOS PARA EL BOTÓN DE DESCARGAR ARCHIVO//////////////////////////////////////////////////////////////////////////
            aplicacion.btnDescargarArchivo.addMouseListener(new MouseListener(){ //escucha activa
                public void mouseClicked(MouseEvent e){                    
                                        
                    //OBTENEMOS LOS ÍNDICES DE LOS ELEMENTOS REMOTOS SELECCIONADOS POR EL USUARIO
                    int[] hola =aplicacion.listaCarpetaRemota.getSelectedIndices();
                    
                    //MOSTRAMOS EN EL LOG, LOS ARCHIVOS QUE SE COMPARTIRÁN
                    aplicacion.logsCarpetaServidor.append("Preparándose para compartir "+ hola.length+" archivos...\n");
                    aplicacion.logsCarpetaCliente.append("Preparándose para descargar "+ hola.length+" archivos...\n");
                    
                    
                    //ALMACENAMOS LOS NOMBRES DE ARCHIVOS SELECCIONADOS
                    aplicacion.listaArchivosRemotosSeleccionados=new String [hola.length]; 
                    
                    for(int i=0;i<hola.length ;i++){
                        aplicacion.listaArchivosRemotosSeleccionados[i]=aplicacion.listaArchivos[hola[i]];
                        
                        
                        
                        //MOSTRAMOS EN EL LOG, LOS ARCHIVOS QUE SE COMPARTIRAN/DESCARGARÁN:                        
                        aplicacion.logsCarpetaServidor.append("Preparándose para compartir el archivo "+aplicacion.listaArchivosRemotosSeleccionados[i]+" \n");
                        aplicacion.logsCarpetaCliente.append("Preparándose para descargar el archivo "+aplicacion.listaArchivosRemotosSeleccionados[i]+" \n");
                        
                    }
                    
                    
                    try {
                        
                        //MANDAMOS A LLAMAR A LA FUNCIÓN QUE DESCARGA MÚLTIPLES ARCHIVOS
                        backendCliente.obtenerMultiplesArchivos(dos,dis, //EL SOCKET
                                aplicacion.listaArchivosRemotosSeleccionados, //la lista de nombres de archivos seleccionados
                                aplicacion.rutaActualArchivos, //la ruta remota actual de los archivos
                                aplicacion.rutaDescargas); //la ruta local donde queremos que se guarde la descarga
                        
                        //NOTIFICAMOS QUE SE HAN DESCARGADO LOS ARCHIVOS
                        aplicacion.logsCarpetaServidor.append("Se han compartido "+ hola.length+" archivos\n");
                        aplicacion.logsCarpetaCliente.append("Se han descargado "+ hola.length+" archivos\n");
                        
                    } catch (IOException ex) {
                        Logger.getLogger(aplicacionCliente.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    
                    //Refrescamos el jfilechooser
                    aplicacion.navegadorCarpetaLocal.setCurrentDirectory(new File("./"));
                    aplicacion.navegadorCarpetaLocal.setCurrentDirectory(new File(aplicacion.rutaDescargas));                                                            
                }
              
                //otros tipos de eventos
                public void mouseEntered(MouseEvent e){} public void mouseExited(MouseEvent e){} public void mousePressed(MouseEvent e){} public void mouseReleased(MouseEvent e){}             
                
            });     
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
            ///////////////////////////EVENTOS PARA EL BOTÓN DE ELIMINAR ARCHIVO REMOTO//////////////////////////////////////////////////////////////////////////
            aplicacion.btnEliminarArchivoRemoto.addMouseListener(new MouseListener(){ //escucha activa
               public void mouseClicked(MouseEvent e){    
                   
                   try {
                       //OBTENEMOS LOS ÍNDICES DE LOS ELEMENTOS REMOTOS SELECCIONADOS POR EL USUARIO
                       int[] hola =aplicacion.listaCarpetaRemota.getSelectedIndices();
                       
                       //MOSTRAMOS EN EL LOG, LOS ARCHIVOS QUE SE COMPARTIRÁN
                       aplicacion.logsCarpetaServidor.append("Preparándose para eliminar "+ hola.length+" archivos...\n");
                       
                       
                       //ALMACENAMOS LOS NOMBRES DE ARCHIVOS SELECCIONADOS A ELIMINAR
                       aplicacion.listaArchivosRemotosSeleccionados=new String [hola.length];
                       
                       for(int i=0;i<hola.length ;i++){ //Guardamos los nombres de archivos a eliminar en un array de cadenas
                           
                               aplicacion.listaArchivosRemotosSeleccionados[i]=aplicacion.listaArchivos[hola[i]];
                               
                               //MOSTRAMOS EN EL LOG, LOS ARCHIVOS QUE SE VAN A ELIMINAR:
                               aplicacion.logsCarpetaServidor.append("Preparándose para eliminar el archivo "+aplicacion.listaArchivosRemotosSeleccionados[i]+" \n");
                               aplicacion.logsCarpetaCliente.append("Solicitando eliminar el archivo "+aplicacion.listaArchivosRemotosSeleccionados[i]+" \n");                                                              
                       
                       }
                       
                       
                       //MANDAMOS A LLAMAR A LA FUNCIÓN QUE ELIMINARÁ LOS MÚLTIPLES ARCHIVOS QUE SE SELECCIONARON
                       backendCliente.eliminarMultiplesArchivosRemotos(dos, //el dataoutput a usar
                               aplicacion.listaArchivosRemotosSeleccionados, //el array con los nombres de los archivos remotos que se desean aliminar
                                aplicacion.rutaActualArchivos); //la ruta remota actual
                       
                       
                       
                       //NOTIFICAMOS QUE SE HAN ELIMINADO LOS ARCHIVOS
                       aplicacion.logsCarpetaServidor.append("Se han ELIMINADO "+ hola.length+" archivos\n");
                       aplicacion.logsCarpetaCliente.append("Se han ELIMINADO "+ hola.length+" archivos\n");
                       
                        for(int i=0;i<hola.length ;i++){
                            //ACTUALIZAMOS EL LISTADO DE LA CARPETA ACTUAL
                            aplicacion.listaArchivos=backendCliente.obtener_ls_remoto(dis);
                            aplicacion.listaCarpetaRemota.setListData(aplicacion.listaArchivos);
                        }                            
                        
                       //Bloqueamos los botones de descargar y eliminar:
                       aplicacion.btnDescargarArchivo.setEnabled(false);
                       aplicacion.btnEliminarArchivoRemoto.setEnabled(false);
                                              
                   } catch (IOException ex) {
                       Logger.getLogger(aplicacionCliente.class.getName()).log(Level.SEVERE, null, ex);
                   }                                                                            
                   
               }
               
               //otros tipos de eventos
                public void mouseEntered(MouseEvent e){} public void mouseExited(MouseEvent e){} public void mousePressed(MouseEvent e){} public void mouseReleased(MouseEvent e){}             
            });     
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            
            ///////////////////////////EVENTOS PARA EL BOTÓN DE VOLVER UNA CARPETA ATRÁS//////////////////////////////////////////////////////////////////////////
            aplicacion.btnAtrasCarpeta.addMouseListener(new MouseListener(){ //escucha activa
                public void mouseClicked(MouseEvent e){                    
                    try {
                        
                        //ENVIAMOS LA PETICION
                        backendCliente.enviaPeticion(dos,5); //petición 5= volver atrás (carpeta padre)
                        
                        //Recibimos el nuevo path (el path del padre)
                        aplicacion.rutaActualArchivos=backendCliente.obtener_path_remoto(dis);
                        
                        //OBTENEMOS LA LISTA DE LA NUEVA CARPETA
                        aplicacion.listaArchivos=backendCliente.obtener_ls_remoto(dis);
                        
                        //ACTUALIZAMOS EL JLIST (LA LISTA MOSTRADA EN LA VENTANA)
                        aplicacion.listaCarpetaRemota.setListData(aplicacion.listaArchivos);
                        
                        //ACTUALIZAMOS EL LOG:
                        aplicacion.logsCarpetaServidor.append("Volviendo una carpeta atrás\n");
                        aplicacion.logsCarpetaServidor.append("Carpeta listada:"+aplicacion.rutaActualArchivos+"\n");
                        
                        
                    } catch (IOException ex) {
                        Logger.getLogger(aplicacionCliente.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }                
                //otros tipos de eventos
                public void mouseEntered(MouseEvent e){} public void mouseExited(MouseEvent e){} public void mousePressed(MouseEvent e){} public void mouseReleased(MouseEvent e){}             
            });     
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            
             ///////////////////////////EVENTOS PARA EL BOTÓN DE CERRAR CONEXIÓN//////////////////////////////////////////////////////////////////////////
             aplicacion.btnCerrarConexion.addMouseListener(new MouseListener(){ //escucha activa
                public void mouseClicked(MouseEvent e){                    

                    //Enviamos al servidor la petición de terminar la conexión
                    try {
                        //Enviamos al servidor la petición de terminar la conexión
                        backendCliente.enviaPeticion(dos,6); //peticion 6= cerrar conexión

                        System.out.println("Conexión finalizada");
                    } catch (IOException ex) {
                        Logger.getLogger(aplicacionCliente.class.getName()).log(Level.SEVERE, null, ex);
                    }                                                

                    aplicacion.dispose();//cerramos la ventana
                    System.out.println("VENTANA CERRADA");

                    //cerramos el socket
                    try {
                        //cerramos el socket
                        dos.close();
                        dis.close();
                        socketCliente.close();
                        System.out.println("SOCKET CERRADO");
                    } catch (IOException ex) {
                        Logger.getLogger(aplicacionCliente.class.getName()).log(Level.SEVERE, null, ex);
                    }                

                }
                
                //otros tipos de eventos
                public void mouseEntered(MouseEvent e){} public void mouseExited(MouseEvent e){} public void mousePressed(MouseEvent e){} public void mouseReleased(MouseEvent e){}             
            });  
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            
            ///////////////////////////EVENTOS PARA EL BOTÓN DE CERRAR VENTANA//////////////////////////////////////////////////////////////////////////
            aplicacion.addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent we){
                    try {
                        //Enviamos al servidor la petición de terminar la conexión
                        backendCliente.enviaPeticion(dos,6);
                        System.out.println("Conexión finalizada");
                    } catch (IOException ex) {
                        Logger.getLogger(aplicacionCliente.class.getName()).log(Level.SEVERE, null, ex);
                    }                                

                    System.out.println("VENTANA CERRADA");

                    try {
                        //cerramos el socket
                        dis.close();
                        dos.close();
                        socketCliente.close();
                        System.out.println("SOCKET CERRADO");
                    } catch (IOException ex) {
                        Logger.getLogger(aplicacionCliente.class.getName()).log(Level.SEVERE, null, ex);
                    }                
                }
            });
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            
            ///////////////////////////EVENTOS PARA EL BOTÓN DE ELIMINAR ARCHIVO LOCAL//////////////////////////////////////////////////////////////////////////
            aplicacion.btnEliminarArchivoLocal.addMouseListener(new MouseListener(){ //escucha activa
                public void mouseClicked(MouseEvent e){    
                    //esto se hace de manera local, no necesita mandar petición al servidor
                                                            
                    //Obtenemos la ruta padre del archivo local seleccionado
                    String carpetaActual=aplicacion.archivosLocalesSeleccionados[0].getParent();
                                        
                    
                    //MANDAMOS A ELIMINAR CADA UNO DE LOS ARCHIVOS EN EL ARREGLO DE ARCHIVOS OBTENIDOS POR EL JFILCHOOSER:
                    for(int i=0;i<aplicacion.archivosLocalesSeleccionados.length;i++){
                        String nombreArchivo=aplicacion.archivosLocalesSeleccionados[i].getPath();
                        backendCliente.eliminarArchivoLocal(nombreArchivo);
                        //ACTUALIZAMOS EL LOG:
                        aplicacion.logsCarpetaCliente.append("Archivo eliminado: "+nombreArchivo+"\n");
                    }
                                                                                                    
                    //ACTUALIZAMOS EL JFILECHOOSER                                                  
                    aplicacion.navegadorCarpetaLocal.setCurrentDirectory(new File("./"));                    
                    aplicacion.navegadorCarpetaLocal.setCurrentDirectory(new File(carpetaActual));
                }
                
                //otros tipos de eventos
                public void mouseEntered(MouseEvent e){} public void mouseExited(MouseEvent e){} public void mousePressed(MouseEvent e){} public void mouseReleased(MouseEvent e){}             
            });        
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////                                      
        
    }//termina main
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////// 

    //Constructor de la Interfaz gráfica (solo la inicializa)///////////////////////////////////////////////////////////////////////////
    public aplicacionCliente(Dimension dimVentana){ //se recibe la dimensión de la ventana                   
                
        
        
        Container cuerpoVentana=getContentPane();
        
        cuerpoVentana.setLayout(new BoxLayout(cuerpoVentana,BoxLayout.Y_AXIS)); //definimos el tipo de posicionamiento para elementos en el cuerpo de la ventana
        
        fontTitulos= new Font("Arial",Font.BOLD, 15 ); //fuente para títulos    
        
        
        
        //EL CUERPO DE LA VENTANA SE DIVIDE VERTIFCALMENTE EN TRES CONTENEDOES
        
        ////PRIMER CONTENEDOR (PRIMERA MITAD)//////////////       
        divLadoServer=new JPanel();                         
        divLadoServer.setBorder(BorderFactory.createLineBorder(Color.BLACK));                       
        divLadoServer.setMaximumSize(new Dimension(dimVentana.width,dimVentana.height*6/14));                        
        divLadoServer.setLayout(new BoxLayout(divLadoServer,BoxLayout.X_AXIS)); //acomodo horizontal        
        cuerpoVentana.add(divLadoServer);         
        
            divCarpetaRemota= new JPanel();//cuadro de contenido para mostrar la carpeta remota                                                 
            divCarpetaRemota.setMaximumSize(new Dimension(60000,divLadoServer.getMaximumSize().height));
            divCarpetaRemota.setLayout(new BoxLayout(divCarpetaRemota,BoxLayout.Y_AXIS));             
            divCarpetaRemota.setBorder(new EmptyBorder(10,10,10,10)); //margen para el contenedor               
            divLadoServer.add(divCarpetaRemota);
            
                tituloRemota=new JLabel("CARPETA REMOTA"); 
                tituloRemota.setFont(fontTitulos);
                
                divCarpetaRemota.add(tituloRemota);                
                    divScrollRemota=new JScrollPane(); //panel con scroll (cuando rebasa los límites)                                        
                    listaCarpetaRemota=new JList<>();
                    divCarpetaRemota.add(divScrollRemota);                        
                        listaCarpetaRemota.setMaximumSize(new Dimension(5000,5000));
                        divScrollRemota.setViewportView(listaCarpetaRemota);
            
            divOpcionesRemota= new JPanel();                    
            divOpcionesRemota.setBackground(new Color(196,226,230));
            divOpcionesRemota.setBorder(new EmptyBorder(10,10,10,10)); //margen para el contenedor
            divOpcionesRemota.setLayout(new BoxLayout(divOpcionesRemota,BoxLayout.Y_AXIS)); //despliegue en vertical
            divOpcionesRemota.setMaximumSize(new Dimension(20000,divLadoServer.getMaximumSize().height));  
            divLadoServer.add(divOpcionesRemota);   
                opcionesCarpetaRemota= new JToolBar(JToolBar.VERTICAL); //barra VERTICAL de herramientas (para los botones)
                opcionesCarpetaRemota.setMaximumSize(new Dimension(divLadoServer.getMaximumSize().width,divLadoServer.getMaximumSize().height));
                opcionesCarpetaRemota.setBackground(new Color(196,226,220));
                opcionesCarpetaRemota.setBorder(new EmptyBorder(50,50, 50, 50));
                opcionesCarpetaRemota.setLayout(new GridLayout(5,1)); //para que los botones (opciones) queden bien posicionadas              
                divOpcionesRemota.add(opcionesCarpetaRemota);
                
                    btnAbrirCarpeta=new JButton("Abrir carpeta");                     
                    //btnAbrirCarpeta.setBorder(new EmptyBorder(20,50,20,50));                    
                    opcionesCarpetaRemota.add(btnAbrirCarpeta);                                        
                    
                    btnDescargarArchivo=new JButton("Descargar");
                    //btnDescargarArchivo.setBorder(new EmptyBorder(20,50,20,50));
                    opcionesCarpetaRemota.add(btnDescargarArchivo);                                        
                    
                    btnAtrasCarpeta=new JButton("Atrás");
                    //btnAtrasCarpeta.setBorder(new EmptyBorder(20,50,20,50));
                    opcionesCarpetaRemota.add(btnAtrasCarpeta);
                    
                    btnCerrarConexion=new JButton("Cerrar conexión");
                    opcionesCarpetaRemota.add(btnCerrarConexion);
                    
                btnEliminarArchivoRemoto=new JButton("Eliminar");
                //btnEliminarArchivoRemoto.setBorder(new EmptyBorder(20,50,20,50));
                divOpcionesRemota.add(btnEliminarArchivoRemoto); //se agrega a parte, porque debe ser más pequeño
                
                    
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        //////////////CONTENEDOR ENMEDIO DE LOS DOS MÁS GRANDES///////////////////////////////////////////////////////////////
        divLogs= new JPanel();
        divLogs.setBorder(BorderFactory.createLineBorder(Color.BLACK));        
        divLogs.setLayout(new GridLayout(1,2));   
        divLogs.setMaximumSize(new Dimension(dimVentana.width,dimVentana.height*2/14)); //                          
        cuerpoVentana.add(divLogs);                  
            divLogServidor=new JPanel();
            divLogServidor.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            divLogServidor.setLayout(new BoxLayout(divLogServidor,BoxLayout.Y_AXIS));
            divLogs.add(divLogServidor);
                tituloLogsServidor=new JLabel("Logs de la carpeta del Servidor");
                divLogServidor.add(tituloLogsServidor);            
                logsServidorScroll= new JScrollPane();                                  
                divLogServidor.add(logsServidorScroll);            
                    logsCarpetaServidor= new JTextArea();
                    logsCarpetaServidor.setEditable(false);
                    logsServidorScroll.setViewportView(logsCarpetaServidor);
            
            divLogCliente=new JPanel();
            divLogCliente.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            divLogCliente.setLayout(new BoxLayout(divLogCliente,BoxLayout.Y_AXIS));
            divLogs.add(divLogCliente);           
                tituloLogsCliente=new JLabel("Logs de la carpeta del cliente");
                divLogCliente.add(tituloLogsCliente);    
                logsClienteScroll=new JScrollPane();
                divLogCliente.add(logsClienteScroll);  
                    logsCarpetaCliente = new JTextArea();      
                    logsCarpetaCliente.setEditable(false);
                    logsClienteScroll.setViewportView(logsCarpetaCliente);
                
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        
        ////SEGUNDO CONTENEDOR ////////////////////////////////////////////////////////////////////////////////////////////
        divLadoCliente=new JPanel();        
        divLadoCliente.setBorder(BorderFactory.createLineBorder(Color.BLACK));        
        divLadoCliente.setLayout(new BoxLayout(divLadoCliente,BoxLayout.X_AXIS));   
        divLadoCliente.setMaximumSize(new Dimension(dimVentana.width,dimVentana.height*6/14)); //          
        
        cuerpoVentana.add(divLadoCliente);                              
        
            divCarpetaLocal= new JPanel(); //cuadro para Guardar el filechooser y el título                                   
            divCarpetaLocal.setMaximumSize(new Dimension(60000,divLadoCliente.getMaximumSize().height));            
            divCarpetaLocal.setLayout(new BoxLayout(divCarpetaLocal,BoxLayout.Y_AXIS));
            divCarpetaLocal.setBorder(new EmptyBorder(10,10,10,10)); //margen para el contenedor
            divLadoCliente.add(divCarpetaLocal);                                
            
                tituloLocal= new JLabel("ARCHIVOS LOCALES");//titulo del recuadro
                tituloLocal.setFont(fontTitulos);
                divCarpetaLocal.add(tituloLocal);  
                
                navegadorCarpetaLocal= new JFileChooser();//selector de archivo
                navegadorCarpetaLocal.setMultiSelectionEnabled(true); //
                navegadorCarpetaLocal.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES); //PARA PODER SELECCIONAR ARCHIVOS Y 
                divCarpetaLocal.add(navegadorCarpetaLocal);
                    
            
            divOpcionesLocal=new JPanel();                        
            divOpcionesLocal.setBackground(new Color(196,226,250));
            divOpcionesLocal.setBorder(new EmptyBorder(10,10,10,10)); //margen para el contenedor
            divOpcionesLocal.setMaximumSize(new Dimension(30000,divLadoCliente.getMaximumSize().height));              
            divOpcionesLocal.setLayout(new BoxLayout(divOpcionesLocal,BoxLayout.Y_AXIS)); //despliegue en vertical
            divLadoCliente.add(divOpcionesLocal);               
                opcionesCarpetaLocal=new JToolBar(JToolBar.VERTICAL);                
                opcionesCarpetaLocal.setMaximumSize(new Dimension(divOpcionesLocal.getMaximumSize().width,divOpcionesLocal.getMaximumSize().height));
                opcionesCarpetaLocal.setBorder(new EmptyBorder(50,50,50,50));
                opcionesCarpetaLocal.setBackground(new Color(196,226,240));                
                opcionesCarpetaLocal.setLayout(new GridLayout(5,1));
                divOpcionesLocal.add(opcionesCarpetaLocal);         
                    btnSubirArchivo=new  JButton("Subir");
                    opcionesCarpetaLocal.add(btnSubirArchivo);
                btnEliminarArchivoLocal=new  JButton("Eliminar");
                divOpcionesLocal.add(btnEliminarArchivoLocal);
                    
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////                                                                                
                    
    }                         
    
}
