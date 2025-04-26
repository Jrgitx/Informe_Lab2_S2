import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator; // se tuvo que importar para que Iterator funcione en UrnaElectoral
import java.util.LinkedList;
import java.util.Stack;
import java.util.Queue;

public class Electo {
    class Voto {
        private int idVoto;//único para cada voto
        private int votanteId;//utilizado para evitar votos duplicados
        private int candidatoId;//ID unico del candidato seleccionado
        private String timeStamp;//Hora del voto en formato “hh:mm:ss”

        //Constructor
        public Voto(int idVoto, int votanteId, int candidatoId, String timeStamp) {
            this.idVoto = idVoto;
            this.votanteId = votanteId;
            this.candidatoId = candidatoId;
            this.timeStamp = timeStamp;
        }

        //getters
        public int getidVoto() {return idVoto;}
        public int getVotanteId() {return votanteId;}
        public int getCandidatoId() {return candidatoId;}
        public String getTimeStamp() {return timeStamp;}

        //setters
        public void setId(int idVoto) {this.idVoto = idVoto;}
        public void setVotanteId(int votanteId) {this.votanteId = votanteId;}
        public void setCandidatoId(int candidatoId) {this.candidatoId = candidatoId;}
        public void setTimeStamp(String timeStamp) {this.timeStamp = timeStamp;}
    }

    class Candidato {
        private int id;
        private String nombre;
        private String partido;
        private Queue<Voto> votosRecibidos; //cola de votos asociados al candidato de tipo Voto

        //Constructor
        public Candidato(int id, String nombre, String partido) {
            this.id = id;
            this.nombre = nombre;
            this.partido = partido;
            votosRecibidos = new LinkedList<Voto>();
        }

        //getters
        public int getId() {return id;}
        public String getNombre() {return nombre;}
        public String getPartido() {return partido;}

        //setters
        public void setId(int id) {this.id = id;}
        public void setNombre(String nombre) {this.nombre = nombre;}
        public void setPartido(String partido) {this.partido = partido;}

        //Añade un voto a la cola de votos del candidato.
        public void agregarVoto(Voto v) {votosRecibidos.add(v);}

        /**
         * Busca en votosRecibidos un voto con idVoto,
         * lo elimina y lo devuelve; si no lo halla, devuelve null.
         */
        public Voto removerVotoPorId(int idVoto) { /** CREADO PARA USARLO EN reportarVoto() */
            Iterator<Voto> it = votosRecibidos.iterator();
            while (it.hasNext()) {
                Voto v = it.next();
                if (v.getidVoto() == idVoto) {
                    it.remove();   // elimina seguro dentro del iterator
                    return v;      // devolvemos el voto para reusar
                }
            }
            return null;
        }

        public int getTotalVotos() { /** CREADO PARA obtenerResultados() */
            return votosRecibidos.size();
        }

    }

    class Votante {
        private int id;
        private String nombre;
        private boolean yaVoto; //booleano para verificar si la persona voto previamente

        //Constructor
        public Votante(int id, String nombre, boolean yaVoto) {
            this.id = id;
            this.nombre = nombre;
            this.yaVoto = yaVoto;
        }

        //getters
        public int getId() {return id;}
        public String getNombre() {return nombre;}
        public boolean getYaVoto() {return yaVoto;}

        //setters
        public void setId(int id) {this.id = id;}
        public void setNombre(String nombre) {this.nombre = nombre;}
        public void setYaVoto(boolean yaVoto) {this.yaVoto = yaVoto;}

        //Cambia yaVoto a true
        public void marcarComoVotado() {yaVoto = true;}
    }

    class UrnaElectoral {
        private LinkedList<Candidato> listaCandidatos;
        private Stack<Voto> historialVotos;
        private Queue<Voto> votosReportados;
        private int idCounter;

        public UrnaElectoral() {
            listaCandidatos = new LinkedList();
            historialVotos = new Stack(); //pila para almacenar votos en orden cronológico inverso (la pila recibirá sólo Votos)
            votosReportados = new LinkedList<Voto>(); //cola para votos anulados o impugnados (La cola recibirá solo Votos)
            idCounter = 1; //contador de IDs para votos
        }

        //Verifica si el votante ya ha votado.
        public boolean verificarVotante(Votante votante) {
            return votante.getYaVoto();
        }

        //Utiliza el metodo verificarVotante
        //luego registra el voto (se tiene que crear un nuevo Voto para agregarlo al Candidato)
        //en el Candidato correspondiente y lo añade al historial. Posteriormente tiene que cambiar el estado del Votante
        public boolean registrarVoto(Votante votante, int candidatoId) {
            // 1) Si ya votó, salimos con false
            if (verificarVotante(votante)) {
                return false;
            } // guard clause o early return

            // 2) Buscamos el candidato en la lista
            for (Candidato candidato : listaCandidatos) { //for-each o "for mejorado", for (Tipo elemento : coleccion)
                if (candidato.getId() == candidatoId) {

                    // 3) Creamos un nuevo Voto
                    String timeStamp = LocalTime.now()
                            .format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                    Voto nuevoVoto = new Voto(idCounter, votante.getId(), candidatoId, timeStamp);
                    // idCounter, id del elector, // id del candidato, // hora en hh:mm:ss

                    idCounter++;

                    // 4) Lo agregamos al candidato
                    candidato.agregarVoto(nuevoVoto);

                    //y al historial
                    historialVotos.push(nuevoVoto);

                    // 5) Marcamos al votante como que ya votó
                    votante.marcarComoVotado();

                    return true;  // voto registrado con éxito
                }
            }

            // Si llegamos aquí, no existe ningún candidato con ese ID
            return false;
        }

        //Mueve un voto de la cola correspondiente al candidato a la cola de votos reportados
        //(Entrega un mensaje en caso de existir ya el voto en la cola por ejemplo, en caso de fraude)
        public boolean reportarVoto(Candidato candidato, int idVoto) {

            for (Voto yaReportado : votosReportados) { /** for-each (for mejorado) para recorrer la cola */
                if (yaReportado.getidVoto() == idVoto) {
                    System.out.println("El voto (ID) " + idVoto + " ya está reportado.");
                    return false;
                }
            }

            // 2) Pedimos al candidato que quite el voto de su cola
            Voto v = candidato.removerVotoPorId(idVoto);
            if (v == null) {
                System.out.println("No existe el voto (ID) " + idVoto +
                        " en la cola del candidato " +
                        candidato.getId());
                return false;
            }

            // 3) Lo añadimos a votosReportados
            votosReportados.add(v);
            System.out.println("Voto (ID) " + idVoto + " reportado exitosamente.");
            return true;
        }

        //Retorna un mapa con el conteo de votos por Candidato.
        public String obtenerResultados() {
            //StringBuilder es una clase de Java diseñada para construir cadenas de texto de manera eficiente

            StringBuilder sb = new StringBuilder("=== Resultados de la Elección ===\n");
            for (Candidato c : listaCandidatos) {
                //El metodo append aniade la representación en texto de su argumento
                //(puede ser otro String, un número, un objeto, etc.) al final de lo que ya contiene el buffer.
                sb.append("Candidato ")
                        .append(c.getNombre())
                        .append(" (")
                        .append(c.getPartido())
                        .append("): ")
                        .append(c.getTotalVotos());
                        if(c.getTotalVotos() == 1){
                            sb.append(" voto\n");
                        } else{
                            sb.append(" votos\n");
                        }
            }
            //sb.toString convierte el contenido acumulado en el StringBuilder en un objeto String inmutable
            return sb.toString();
        }
    }

    public static void main(String[] args) {
        int contadorDeVotos = 0; // auxiliar para saber el numero correspondiente del voto registrado

        System.out.println("\n"+"---------------------------------------");
        Electo electo = new Electo();
        // 1) Creamos la urna
        UrnaElectoral urna = electo.new UrnaElectoral();

        // 2) Añadimos algunos candidatos
        Candidato c1 = electo.new Candidato(1, "Chamelo Gas", "Partido A");
        Candidato c2 = electo.new Candidato(2, "Bob Patiño",   "Partido B");
        Candidato c3 = electo.new Candidato(3, "Kanye West", "Partido C");
        // Como estamos dentro de la clase Electo, podemos acceder a la lista privada:
        urna.listaCandidatos.add(c1);
        urna.listaCandidatos.add(c2);
        urna.listaCandidatos.add(c3);

        // 3) Creamos votantes
        Votante v1 = electo.new Votante(1, "Juan",  false);
        Votante v2 = electo.new Votante(2, "María", false);
        Votante v3 = electo.new Votante(3, "José", false);

        // 4) Registramos votos
        contadorDeVotos++;
        System.out.println("Voto (ID) " + contadorDeVotos +  " del votante " + v1.getNombre()
                + " registrado: " + urna.registrarVoto(v1, 1)); // true
        contadorDeVotos++;
        System.out.println("Voto (ID) " + contadorDeVotos +  " del votante " + v2.getNombre()
                + " registrado: " + urna.registrarVoto(v2, 2)); // true
        contadorDeVotos++;
        System.out.println("Voto (ID) " + contadorDeVotos +  " del votante " + v3.getNombre()
                + " registrado: " + urna.registrarVoto(v3, 2));

        //Imprimimos para ver los resultados antes de reportar y anular votos
        System.out.println("---------------------------------------"+"\n" + urna.obtenerResultados()
                + "---------------------------------------"+"\n" );

        /** Intento de voto duplicado por parte de Juan (v1), candidato 1 (c1), id (1) */
        System.out.println(v1.getNombre() + " esta votando otra vez: " + urna.registrarVoto(v1, 1) + "\n"); // false

        // 5) Reportamos (anulamos) un voto
        urna.reportarVoto(c1, 1); // mueve el voto con idVoto=1 de c1 a votosReportados
        urna.reportarVoto(c1,1); //id del voto de Juan es 1, pero ya se habia reportado anteriormente
        System.out.println("\n" + v3.getNombre() + " esta votando otra vez: " + urna.registrarVoto(v3, 3));
        urna.reportarVoto(c2, 3); //id del voto de José es 3

        // 6) Imprimimos resultados
        System.out.println("---------------------------------------"+"\n" + urna.obtenerResultados()
                + "---------------------------------------"+"\n" );

        /** Tanto 'Juan' como 'José' intentaron votar de nuevo, lo que ocasiona que se anulen ambos votos
         * Por lo tanto, el partido A: 1 voto -> 0 votos y Partido B: 2 votos -> 1 voto
         */
    }
}