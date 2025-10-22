/*
AUTOR: TONI PADIAL PONS
15/10/2024
 */
package practicapapanoel;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PracticaPapaNoel implements Runnable{
    
    static int numRen=1;
    static int dubtesResolts = 0;
    static int elfsDinsSala = 0;
    //semafor que controla el numero de dubtes
    static Semaphore salaEspera = new Semaphore(3);
    //semafor que permet enganxar els rens al trineu
    static Semaphore permisEnganxar = new Semaphore(0);
    //semafor que controla el numero de dubtes
    static Semaphore permisConstruir = new Semaphore(0);
    //semafor que controla l'acces al jutjat
    static Semaphore PermisResoldreDubtes = new Semaphore(0);
    //semafor que permet despertar al pare Noel
    static Semaphore PermisDespertar = new Semaphore(0);
    //semafor que permet continuar a Pare Noel quan les joguines estan acabades
    static Semaphore joguinesAcabades = new Semaphore(0);
    //semafor que fa esperar a que tots els rens hagin arribat
    static Semaphore rensArribats = new Semaphore(0);
    
    static Semaphore mutex = new Semaphore(1);
    
    static final int FILS = 16;
    static final int RENS = 9;
    static final int ELFS = 6;
    static boolean fiDubtes = false;
    int tipusFil; // 0 = elf, 1 = ren, 2 = pare noel
    String id;
    static final String []noms  ={"RUDOLPH","BLITZEN","DONDER","CUPID","COMET",
        "VIXEN","PRANCER","DANCER","DASHER","Taleasin","Halafarin","Ailduin","Adamar",
        "Galather","Estelar","Pare Noel"};
    
    
    public PracticaPapaNoel(String id, int tipusFil) {
        this.id = id;
        this.tipusFil = tipusFil;
    }
    
     @Override
    public void run() {
        switch(this.tipusFil){
            case 0: // elfs
                try {
                elf(id);
                } catch (Exception ex) {
                Logger.getLogger(PracticaPapaNoel.class.getName()).log(Level.SEVERE, null, ex);
            }
            break;
            
            case 1: //rens
                try {
                ren(id);
                } catch (Exception ex) {
                Logger.getLogger(PracticaPapaNoel.class.getName()).log(Level.SEVERE, null, ex);
            }
            break;
            
            case 2: //Pare Noel
                try {
                Noel(id);
                } catch (Exception ex) {
                Logger.getLogger(PracticaPapaNoel.class.getName()).log(Level.SEVERE, null, ex);
            }
            break;
        }
       
    }
    
    
    private static int elf(String id) throws InterruptedException{
        int numJoguines = 3;
        int joguinesConstruides = 1;
        int numDubtes = ELFS *3;
        try {
            Thread.sleep((long) (Math.random() * 500));
            System.out.println("Hola som l'elf: "+ id+ " construiré "+numJoguines+" joguines" );
            
            while(numJoguines > 0){
                Thread.sleep((long) (Math.random() * 500));
                salaEspera.acquire();
                System.out.println(id+" diu: tinc dubtes amb la joguina "+joguinesConstruides);
                joguinesConstruides++;
                mutex.acquire();
                if(elfsDinsSala == 2){
                    mutex.release();
                    System.out.println(id + " diu: Som 3 que tenim dubtes, PARE NOEEEEEL!");
                    PermisDespertar.release(4);
                    Thread.sleep((long) (Math.random() * 500));
                    PermisResoldreDubtes.release(); //despertamos al PAre Noel
                    mutex.acquire();
                    dubtesResolts = dubtesResolts+3;
                    if(dubtesResolts == numDubtes){
                        fiDubtes = true;
                    }
                    elfsDinsSala=0;
                    mutex.release();
                    salaEspera.release(3);
                }else{
                    elfsDinsSala++;
                    mutex.release();
                    Thread.sleep((long) (Math.random() * 500));
                    PermisDespertar.acquire();
                }
                
                permisConstruir.acquire();
                numJoguines--;
                System.out.println(id + " diu: Construeixo la joguina amb ajuda");
                
            }
            
            System.out.println("L'elf "+ id + " ha fet les seves joguines i acaba <---------" );
            mutex.acquire();
                if(dubtesResolts == numDubtes){
                        System.out.println(id+ " diu: Som el darrer avisaré al Pare Noel");
                        joguinesAcabades.release();
                        
                    }
                mutex.release();
                
        } catch (Exception ex) {
            Logger.getLogger(PracticaPapaNoel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 1;
        
    }
    
    private static int ren(String id) throws InterruptedException{
        try {
            System.out.println(id+" se'n va a pasturar ");
            Thread.sleep((long) (Math.random() * 3000));
            
            mutex.acquire();
            if(numRen<9){
                mutex.release();
                System.out.println("El ren "+ id+ " arriba, "+ numRen);
            }else{
                mutex.release();
                System.out.println("El ren "+id +" diu: Som el darrer en voler podem partir");
                rensArribats.release();//en aquest cas vol dir que els rens han arribat
            }
            
            mutex.acquire();
            numRen++;
            mutex.release();
            
            permisEnganxar.acquire();
            System.out.println("El ren "+ id+ " està enganxat al trineu");
            numRen--;
            permisEnganxar.release();//comença a enganxar els rens
            if(numRen== 1){
                joguinesAcabades.release();
            }
            
            
        } catch (Exception ex) {
            Logger.getLogger(PracticaPapaNoel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 1;
    }
    
    private static int Noel(String id) throws InterruptedException{
        
        try {
            System.out.println("-------> El Pare Noel diu: Estic despert però me'n torn a jeure");
            
            while(!fiDubtes == true){
                
                PermisResoldreDubtes.acquire();
                System.out.println("-------> El Pare Noel diu: Atendré els dubtes d'aquests 3");
                permisConstruir.release(6);
                System.out.println("-------> El Pare Noel diu: Estic cansat me'n torn a jeure");
            }
            
            //esper a que totes les joguines estiguin llestes
            joguinesAcabades.acquire();
            System.out.println("-------> Pare Noel diu: Les joguines estan llestes. I Els rens?");
            
            //esper a que tots els rens estiguin llests
            rensArribats.acquire();
            System.out.println("-------> Pare Noel diu: Enganxaré els rens i partiré");
            permisEnganxar.release();//comença a enganxar els rens
            
            //esper a que tots els rens estiguin enganxats
            joguinesAcabades.acquire();
            System.out.println("-------> El Pare Noel ha enganxat els rens, ha carregat les joguines i se'n va");
            
            
            
            
        } catch (Exception ex) {
            Logger.getLogger(PracticaPapaNoel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 1;
        
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("SIMULACIÓ DEL PARE NOEL I ELS ELFS EN PRÀCTIQUES");
        Thread[] fils = new Thread[FILS];
        
        for (int i = 0; i < fils.length; i++) {
            if (i < RENS) {
                fils[i] = new Thread(new PracticaPapaNoel(noms[i],1));
            } else if (i< RENS + ELFS) {
                fils[i] = new Thread(new PracticaPapaNoel(noms[i],0));
            }
            else{
                fils[i] = new Thread(new PracticaPapaNoel(noms[i],2));
            }
            fils[i].start();
        }
      
      
      for (int i = 0; i < fils.length; i++) {
            fils[i].join();
        }
        
    }
    
}
