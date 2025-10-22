/*
AUTOR: TONI PADIAL PONS
03/11/2023

*/



import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Jutjat implements Runnable{
    //semafor que controla l'acces al jutjat
    static Semaphore jutjat = new Semaphore(1);
    //semafor que bloquetja els sospitosos entre ells
    static Semaphore permisFitxar = new Semaphore(1);
    static Semaphore jutge = new Semaphore(0);
    //semafor que dona permis als sospitosos per declarar
    static Semaphore permisDeclarar = new Semaphore(0);
    //semafor que dona permis als sospitosos per acabar
    static Semaphore permisAcabar = new Semaphore(0);
    static final int SOSPITOSOS = 20;
    //numero de sospitosos dins la sala
    static volatile int sospitososSala=0;
    //numero de sospechosos fichados
    static volatile int numFitxats=0;
    //numero de declaraiones
    static volatile int numDeclaracions = 0;
    static boolean fin = false;
    String id;
    static final String []noms  ={"Deadshot","Harley Quinn","Penguin","Riddler","Bane"
    ,"Talia al Ghul","Ra's al Ghul","Hugo Strange:","Killer Croc","Catwoman",
    "Poison Ivy","Mr. Freeze","Jason Todd","Hush","Joker",
    "Clayface","Deathstroke","Mad Hatter","Two-Face","Scarecrow"};
    
public Jutjat(String id) {
        this.id = id;
    }

    @Override
    public void run() {
        if (id == "Jutge Dredd") {
            try {
                jutge();
            } catch (Exception ex) {
                Logger.getLogger(Jutjat.class.getName()).log(Level.SEVERE, null, ex);
            }
            } else {
            try {
                sospitos(id);
            } catch (InterruptedException ex) {
                Logger.getLogger(Jutjat.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
    }
    
    
    
    
    private static int sospitos(String id) throws InterruptedException{
        try {
            //variable que guarda si un sospitos ha declarat o no
            boolean declarat = false;
            boolean botar = false;
            
            //inici dels sospitosos
            Thread.sleep((long) (Math.random() *500));
            System.out.println("     "+id+": Som innocent!");
            
            //els sospitos entra al jutjat
            Thread.sleep((long) (Math.random() * 500));
            //se incrementa el numero de sospechosos en la sala
            jutjat.acquire();
            if(fin){
            //els sospitos entra al jutjat
            Thread.sleep((long) (Math.random() * 100));
            acabar(declarat,id);
            jutjat.release();
            return 0;
            }else{
                sospitososSala++;
                jutjat.release();
                System.out.println("     "+id+" entra al jutjat.  Sospitosos: "+sospitososSala);
            }
            
            
            //el sospitos fitxa
            Thread.sleep((long) (Math.random() * 500));
            permisFitxar.acquire();
            //amb aquest if feim que tots els fils que han que han quedat bloquetjats
            //perque el jutge ha entrat botin directament al final quan acaben
            
            numFitxats++;
            //miram si ja estan tots els sospitosos de la sala fitxats si es així
            //alliberam al jutge per a que continui prenint declaracio als sopitosos
            if(numFitxats==sospitososSala){
                System.out.println("     "+id+" fitxa.  Fitxats: "+numFitxats);
                jutge.release();
            }else{
                permisFitxar.release();
                System.out.println("     "+id+" fitxa.  Fitxats: "+numFitxats);
            }
            
            
            //els sospitosos declaran
            Thread.sleep((long) (Math.random() * 500));
            permisDeclarar.acquire();
            numDeclaracions++;
            declarat = true;
            if(numDeclaracions==numFitxats){
                System.out.println("     "+id+" declara.  Declaracions: "+numDeclaracions);
                jutge.release();
            }else{
                System.out.println("     "+id+" declara.  Declaracions: "+numDeclaracions);
                permisDeclarar.release();
            }
            
            //els sospitosos acaben
            Thread.sleep((long) (Math.random() * 500));
            //finalment el sospitos acaba
            acabar(declarat, id);
        } catch (Exception ex) {
            Logger.getLogger(Jutjat.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 1;
    }

    
    //metodo que realiza las acciones finales de los sospechosos
    private static void acabar(boolean declarat, String id)throws Exception{
        permisAcabar.acquire();
       if(declarat != true){
           System.out.println("     "+id+" No és just vull declarar! Som innocent");
       }else{
           System.out.println("     "+id+" entra a l'Asil d' Arkham");
       }
       permisAcabar.release();
    }
    
    
    //hilo del juez
    private static void jutge()throws Exception{
        //comienza el juez
        Thread.sleep((long) (Math.random() * 500));
        System.out.println("---->Jutge Dredd: Jo som la llei!");
        
        //entra el juez
        Thread.sleep((long) (Math.random() * 500));
        jutjat.acquire();
        System.out.println("---->Jutge Dredd: Som a la sala, tanqueu porta!");
        
        //verificamos que hay sospechosos en la sala 
        if(sospitososSala>0){
            System.out.println("---->Jutge Dredd: Fitxeu als sospitosos presents");
        
        //empieza las declaraciones
        Thread.sleep((long) (Math.random() * 500));    
            jutge.acquire();
            //els sospitosos poden començar a declarar
            System.out.println("---->Jutge Dredd: Preniu declaració als presents");
            permisDeclarar.release();            
            
        }else{
            jutge.release();
            System.out.println("---->Jutge Dredd: Si no hi ha ningú me'n vaig");
        }
       
        //el jutge envia tothom a l'asil
        jutge.acquire();
        System.out.println("---->Jutge Dredd: Podeu abandonar la sala tots a l'asil!");
        System.out.println("---->Jutge Dredd: La justícia descansa, demá prendré declaració als sospitosos que queden.");
        fin=true;
        jutjat.release();
        //el jutge dona permis als sospitosos per ascabar
        permisAcabar.release();
            
            
    }
    
    public static void main(String[] args) throws InterruptedException{
        System.out.println("La policia de Gotham ha detingut a 20 sospitosos");
        System.out.println("El jutge prendrá declaració als que pugui");
      Thread[] filsSospitosos = new Thread[SOSPITOSOS+1];
      
      for (int i = 0; i < filsSospitosos.length; i++) {
            if (i < SOSPITOSOS) {
                filsSospitosos[i] = new Thread(new Jutjat(noms[i]));
            } else {
                filsSospitosos[i] = new Thread(new Jutjat("Jutge Dredd"));
            }
            filsSospitosos[i].start();
        }
      
      
      for (int i = 0; i < filsSospitosos.length; i++) {
            filsSospitosos[i].join();
        }
    }
    
}
