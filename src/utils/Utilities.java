package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by aldazj on 05.10.15.
 */
public class Utilities {


    /**
     * Affiche une matrice
     * @param matrix : matrice à afficher
     */
    public static void printMatrix(int[][] matrix){
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                System.out.print(matrix[i][j] + "\t");
            }
            System.out.println("");
        }
    }

    /**
     * Récupère la position d'un objet dans le tableau
     * @param array : Tableau d'objets
     * @param value : objet à trouver l'indice
     * @return : la position de l'objet dans le tableau
     */
    public static int getPositionObject(int[] array, int value){
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value){
                return i;
            }
        }
        return -1;
    }

    /***
     * Print best solutions
     * @param bests
     */
    public static void printBestSolutions(ArrayList<FitnessObject> bests){
        for (int i = 0; i < bests.size(); i++) {
            System.out.println(bests.get(i).getSequence()+":"+bests.get(i).getFitness());
        }
    }

    /**
     * Convertion d'un String vers un tableau de int
     * @param array : string à convertir
     * @return : le tableau de int
     */
    public static int[] string_to_intArray(String array){
        String[] numberStrs = array.split(",");
        int[] intArray = new int[numberStrs.length];
        for (int i = 0; i < numberStrs.length; i++) {
            intArray[i] = Integer.parseInt(numberStrs[i].replaceAll("\\D", ""));
        }
        return  intArray;
    }

    /**
     * Convertion d'un String vers un tableau de double
     * @param array : string à convertir
     * @return : le tableau de double
     */
    public static double[] string_to_doubleArray(String array){
        String[] numberStrs = array.split(",");
        double[] intArray = new double[numberStrs.length];
        for (int i = 0; i < numberStrs.length; i++) {
            intArray[i] = Double.parseDouble(numberStrs[i].replaceAll("\\D", ""));
        }
        return  intArray;
    }

    /**
     * Permute deux objets d'un tableau
     * @param p : tableau original
     * @param mvt : mouvements à effectuer
     * @return : tableau qui a permuté les deux éléments
     */
    public static int[] permute(int[] p, String mvt){
        int[] arrayPermute = Arrays.copyOf(p, p.length);
        int[] my_mvt = string_to_intArray(mvt);
        int valueTmp = arrayPermute[my_mvt[0]-1];
        arrayPermute[my_mvt[0]-1] = arrayPermute[my_mvt[1]-1];
        arrayPermute[my_mvt[1]-1] = valueTmp;
        return  arrayPermute;
    }

    /**
     * Initialisation de notre liste tabou
     * @param tabu : liste tabou
     */
    public static void init_Matrix2D(int[][] tabu){
        for (int i = 0; i < tabu.length; i++) {
            Arrays.fill(tabu[i], 0);
        }
    }

    /**
     * Nous permet de trouver des prochain candidats même si leur fitness
     * empire meilleure fitness courante
     * @param candidat : Ancien candidat tabou
     * @param neighbors : tous les voisins d'une solution courante
     * @return : les nouveaux candidats potentiels
     */
    public static ArrayList<FitnessObject> searchNewCandidats(FitnessObject candidat, ArrayList<FitnessObject> neighbors){
        ArrayList<FitnessObject> old_neighbors = new ArrayList<FitnessObject>(neighbors);
        ArrayList<FitnessObject> new_neighbors = new ArrayList<FitnessObject>();
        int index = -1;
        boolean firstCandidat = true;
        double tmpFitness = Double.MAX_VALUE;
        String currentPermutation;

        //delete old candidat
        for (int i = 0; i < neighbors.size(); i++) {
            if(candidat.getSequence().equals(neighbors.get(i).getSequence()) && candidat.getFitness() == neighbors.get(i).getFitness()){
                index = i;
                break;
            }
        }
        old_neighbors.remove(index);

        //Found new candidats
        for (int i = 0; i < old_neighbors.size(); i++) {
            if(old_neighbors.get(i).getFitness() < tmpFitness){
                tmpFitness = old_neighbors.get(i).getFitness();
                currentPermutation = old_neighbors.get(i).getSequence();
                if(firstCandidat){
                    firstCandidat = false;
                }else{
                    new_neighbors.clear();
                }
                new_neighbors.add(new FitnessObject(old_neighbors.get(i).getFitness(), currentPermutation));
            }else if(old_neighbors.get(i).getFitness() == tmpFitness){
                if(!firstCandidat){
                    new_neighbors.add(new FitnessObject(tmpFitness, old_neighbors.get(i).getSequence()));
                }
            }
        }
        return new_neighbors;
    }

    /**
     * Crée une solution de départ aléatoire
     * @param n : taille de la solution de départ
     * @return : une solution aléatoire
     */
    public static int[] initSolution(int n){
        int[] initS = new int[n];
        for (int i = 0; i < n; i++) {
            initS[i] = i+1;
        }
        Random random = new Random();
        for (int i = 0; i < n; i++) {
            int value_index = random.nextInt(n);
            int tmp_value = initS[i];
            initS[i] = initS[value_index];
            initS[value_index] = tmp_value;
        }
        return initS;
    }

    /**
     * Obtient les mouvements possibles
     * @param N : taille de la solution courante
     * @return : les mouvements permis
     */
    public static ArrayList<int[]> getMoves(int N){
        ArrayList<int[]> neighbors_permu = new ArrayList<int[]>();
        int pointer = 0;
        for (int i = 0; i < (int)((N*(N-1))/2); i++) {
            for (int j = pointer; j < N; j++) {
                if(j != pointer){
                    int[] indice = new int[2];
                    indice[0] = pointer+1;
                    indice[1] = j+1;
                    neighbors_permu.add(indice);
                }
            }
            pointer++;
        }
        return neighbors_permu;
    }

    /**
     * Calcule la variance
     * @param mean : moyenne
     * @param fitness : fitness
     * @return : valeur de la variance
     */
    public static double computeVariance(double mean, double[] fitness){
        double var = 0.0;
        for (int i = 0; i < fitness.length; i++) {
            var += Math.pow(fitness[i]-mean, 2);
        }
        return var;
    }

    /***
     * Calcule la variance
     * @param timesExec : time to exec
     * @return
     */
    public static long computeMean(long[] timesExec){
        long mean = 0;
        for (int i = 0; i < timesExec.length; i++) {
            mean += timesExec[i];
        }
        return  mean/timesExec.length;
    }

    public static int[] check_force(int[][] tabu, int u, int l, int currentIteration){
        int[] forceMvt = new int[2];
        boolean foundForce = false;
        int deltaTime = currentIteration + l - u;
        if(currentIteration > u){
            for (int i = 0; i < tabu.length; i++) {
                for (int j = 0; j < tabu[0].length; j++) {
                    if(tabu[i][j] <= (deltaTime)){
                        forceMvt[0] = i+1;
                        forceMvt[1] = j+1;
                        foundForce = true;
                        break;
                    }
                }
                if(foundForce){
                    break;
                }
            }
        }
        if(foundForce){
            return forceMvt;
        }else{
            return null;
        }
    }
}
