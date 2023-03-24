package Tp1;

import java.io.*;
import java.net.*;
import java.util.logging.*;
public class ServidorCentralHilo extends Thread {
    private Socket socket, skHoroscopo, skClima;
    private DataOutputStream dosCliente, dosHoroscopo, dosClima;
    private DataInputStream disCliente, disHoroscopo, disClima;

    private Cache cache;
    private int idSession;
    public ServidorCentralHilo(Socket socket, int id, Cache cache) {
        this.socket = socket;
        this.idSession = id;
        this.cache = cache;

        try {
            dosCliente = new DataOutputStream(socket.getOutputStream());
            disCliente = new DataInputStream(socket.getInputStream());

            //Cada servidor debe arrancar en un puerto diferente, aca hacemos referencia al puerto al cual nos queremos conectar
            skHoroscopo = new Socket("127.0.0.1", 20000);
            skClima = new Socket("127.0.0.1", 20001);

            dosHoroscopo = new DataOutputStream(skHoroscopo.getOutputStream());//buffer de salida
            disHoroscopo = new DataInputStream(skHoroscopo.getInputStream());//buffer de entrada

            dosClima = new DataOutputStream(skClima.getOutputStream());//buffer de salida
            disClima = new DataInputStream(skClima.getInputStream());//buffer de entrada

        } catch (IOException ex) {

            Logger.getLogger(ServidorCentralHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void desconnectar() {
        try {
            socket.close();
            skHoroscopo.close();
            skClima.close();
        } catch (IOException ex) {
            Logger.getLogger(ServidorCentralHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @Override
    public void run() {
        String accion = "";
        try {
            accion = disCliente.readUTF();
            System.out.println("Consulta de cliente para servidorCentralHilo"+idSession+": "+accion);
            String[] consultas=separadorConsultas(accion);//[0]=signo,[1]=fecha
            String respuestaSigno = consultaHoroscopo(consultas[0]);
            String respuestaClima = consultaClima(consultas[1]);
            dosCliente.writeUTF(respuestaSigno+" y "+respuestaClima );//respuesta final para cliente
        } catch (IOException ex) {
            Logger.getLogger(ServidorCentralHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
        desconnectar();
    }

    private String consultaHoroscopo(String signo) {
        String respuesta="";
        try {
            respuesta= cache.getConsulta(signo);
            if(respuesta==null){
                dosHoroscopo.writeUTF(signo);
                respuesta = disHoroscopo.readUTF();
                cache.putRespuesta(signo, respuesta);
            }
        } catch (IOException ex) {
            Logger.getLogger(Persona.class.getName()).log(Level.SEVERE, null, ex);
        }
        return respuesta;
    }

    private String consultaClima(String fecha) {
        String respuesta="";
        try {
            respuesta= cache.getConsulta(fecha);
            if(respuesta==null){
                dosClima.writeUTF(fecha);
                respuesta = disClima.readUTF();
                cache.putRespuesta(fecha, respuesta);
            }
        } catch (IOException ex) {
            Logger.getLogger(Persona.class.getName()).log(Level.SEVERE, null, ex);
        }
        return respuesta;
    }

    private String[] separadorConsultas(String consultaOrigen){
        String consultaLowerCase= consultaOrigen.toLowerCase();
        String[] consultasSeparadas=consultaLowerCase.split("-");
        return consultasSeparadas;
    }
}
