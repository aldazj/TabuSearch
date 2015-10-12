package main;

import utils.FitnessObject;
import utils.ReadFile;
import utils.Utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by aldazj on 05.10.15.
 */
public class TabuSearch {

    private int[][] tabu;
    private int[][] W;
    private  int[][] D;
    private int N;
    private int[][] diversificationMatrix;
    private int u_iter;

    public TabuSearch(String path_filename) {
        initialisation(path_filename);
        int[] tabu_ternure_l = new int[]{1, (int)(0.5*N), (int)(0.9*N)};
//        int[] tabu_ternure_l = new int[]{(int)(0.5*N)};

        boolean[] executionDiversification = new boolean[]{false};
        int times_algo = 10;
        ArrayList<FitnessObject> bestSolutions;
        int t_max = 20000;

        for (int l = 0; l < tabu_ternure_l.length; l++) {
            for (int k = 0; k < executionDiversification.length; k++) {
                bestSolutions = new ArrayList<FitnessObject>();
                Utilities.init_Matrix2D(diversificationMatrix);
                long[] tempsExec = new long[times_algo];
                long startTime, stopTime, meanTime;

                for (int i = 0; i < times_algo; i++) {
                    startTime = System.currentTimeMillis();
                    tabu = new int[N][N];
                    Utilities.init_Matrix2D(tabu);
                    FitnessObject best = main(tabu_ternure_l[l], executionDiversification[k], t_max);
                    bestSolutions.add(best);
                    stopTime = System.currentTimeMillis();
                    tempsExec[i] = stopTime - startTime;
                }
                double tmpFitness, var, mean = 0.0;
                double bestSolutionFitness = Double.MAX_VALUE;
                String bestSolutionSequence = "", tmpSequence = "";
                double[] fitness = new double[bestSolutions.size()];
                for (int i = 0; i < bestSolutions.size(); i++) {
                    tmpFitness = bestSolutions.get(i).getFitness();
                    tmpSequence = bestSolutions.get(i).getSequence();
                    mean += tmpFitness;
                    fitness[i] = tmpFitness;
                    if(tmpFitness < bestSolutionFitness){
                        bestSolutionFitness = tmpFitness;
                        bestSolutionSequence = tmpSequence;
                    }
                }
                mean = mean/bestSolutions.size();
                var = Utilities.computeVariance(mean, fitness);
                meanTime = Utilities.computeMean(tempsExec);

                System.out.println("------------------ "+times_algo+" Solutions tabuTernure: "+tabu_ternure_l[l]+" ------------------ ");
                Utilities.printBestSolutions(bestSolutions);
                System.out.println("-------------------------------------------------------------- ");
                System.out.println("The best: "+bestSolutionSequence+" Fitness: "+bestSolutionFitness);
                System.out.println("The mean: "+mean);
                System.out.println("The standard deviation "+Math.sqrt(var));
                System.out.println("Execution time mean : "+meanTime);
            }
        }
    }

    /**
     * Lance l'algorithme
     * @param tabuTernure
     * @param diversification
     * @param t_max
     * @return
     */
    public FitnessObject main(int tabuTernure, boolean diversification, int t_max){
        String theBestSequence = "";
        double theBestFitness = Double.MAX_VALUE;
        int p0[] = Utilities.initSolution(N);
        double fitness = compute_fitness(W, D, p0);
        ArrayList<int[]> mvts = Utilities.getMoves(N);

        for (int iteration = 0; iteration < t_max ; iteration++) {
            boolean foundCandidat = false;
            boolean foundMvtVerif = false;
            int[] forceMvt = null;
            if (diversification) {
                forceMvt = Utilities.check_force(tabu, u_iter, tabuTernure, iteration);
                if (forceMvt != null) {
                    foundMvtVerif = true;
                }
            }

            if (foundMvtVerif) {
                int objetc_i = forceMvt[0];
                int position_i = Utilities.getPositionObject(p0, objetc_i);
                int position_j = forceMvt[1]-1;
                int object_j = p0[position_j];
                int[] mvt_tmp = new int[2];
                mvt_tmp[0] = position_i+1;
                mvt_tmp[1] = position_j+1;
                p0 = Utilities.permute(p0, Arrays.toString(mvt_tmp));
                tabu[objetc_i-1][position_i] = iteration+tabuTernure+1;
                tabu[object_j-1][position_j] = iteration+tabuTernure+1;
                fitness +=  fitnessVariation(W, D, p0, mvt_tmp);
            }else{
                ArrayList<FitnessObject> allNeighbors = new ArrayList<FitnessObject>();
                ArrayList<FitnessObject> neighborsCandidat =  neighbors(W, D, p0, mvts, allNeighbors);
                int nbTest = 0;
                do {
                    //Best candidats
                    FitnessObject candidat = null;
                    for (int j = 0; j < neighborsCandidat.size(); j++) {
                        candidat = neighborsCandidat.get(j);
                        nbTest += 1;
                        if (!isTabuNeighbor(p0, candidat, iteration, tabuTernure) || theBestFitness+candidat.getFitness() < theBestFitness) {
                            p0 = Utilities.permute(p0, candidat.getSequence());
                            fitness += candidat.getFitness();
                            theBestFitness = fitness;
                            theBestSequence = Arrays.toString(p0);
                            foundCandidat = true;
                            break;
                        }
                    }
                    if(!foundCandidat){
                        neighborsCandidat = Utilities.searchNewCandidats(candidat, allNeighbors);
                    }
                }while(!foundCandidat && nbTest <= N);
            }
        }
        return new FitnessObject(theBestFitness, theBestSequence);
    }

    /**
     *
     * @param W : matrice de flux
     * @param D : matrice de distances
     * @param p : solution courante
     * @param mvts : ensemble de mouvements
     * @param allNeighbors : récupère tous les voisins existants
     * @return : les meilleurs candidat
     */
    private ArrayList<FitnessObject> neighbors(int[][] W, int[][]D, int[] p, ArrayList<int[]> mvts, ArrayList<FitnessObject> allNeighbors){
        ArrayList<FitnessObject> bestsCandidats = new ArrayList<FitnessObject>();
        boolean firstCandidat = true;
        double tmpFitness = Double.MAX_VALUE;
        String currentPermutation;
        for (int i = 0; i < mvts.size(); i++) {
            double deltaFitnessVar = fitnessVariation(W, D, p, mvts.get(i));
            allNeighbors.add(new FitnessObject(deltaFitnessVar, Arrays.toString(mvts.get(i))));
            if(deltaFitnessVar < tmpFitness){
                tmpFitness = deltaFitnessVar;
                currentPermutation = Arrays.toString(mvts.get(i));
                if(firstCandidat){
                    firstCandidat = false;
                }else{
                    bestsCandidats.clear();
                }
                bestsCandidats.add(new FitnessObject(deltaFitnessVar, currentPermutation));
            }else if(deltaFitnessVar == tmpFitness){
                if(!firstCandidat){
                    bestsCandidats.add(new FitnessObject(tmpFitness, Arrays.toString(mvts.get(i))));
                }
            }
        }
        return bestsCandidats;
    }


    /**
     * Calcule la variation de la fitness pour un voisin donné
     * @param W : Matrice des flux
     * @param D : Matrice des distances
     * @param p : solution courante
     * @param m : mouvements
     * @return
     */
    private double fitnessVariation(int[][] W, int[][]D, int[] p, int[] m){
        double deltaFitness = 0.0;
        int i = p[m[0]-1], j = p[m[1]-1];
        for (int k = 1; k < p.length+1; k++) {
            if(k != i && k != j){
                deltaFitness += 2*(W[j-1][k-1]-W[i-1][k-1])*
                        (D[Utilities.getPositionObject(p, i)][Utilities.getPositionObject(p,k)] -
                                D[Utilities.getPositionObject(p, j)][Utilities.getPositionObject(p, k)]);
            }
        }
        return deltaFitness;
    }

    /**
     * Calcule de la fitness
     * @param W : Matrice des flux
     * @param D : Matrice des distances
     * @param p0 : solution courante
     * @return : la fitness
     */
    private double compute_fitness(int[][] W, int[][] D, int[] p0){
        double fitness = 0.0;
        for (int i = 1; i < W.length+1; i++) {
            for (int j = 1; j < W[0].length+1; j++) {
                if(i != j){
                    fitness += W[i-1][j-1]*D[Utilities.getPositionObject(p0, i)][Utilities.getPositionObject(p0, j)];
                }
            }
        }
        return fitness;
    }

    /***
     * Vérifie si un voisin mouvement est tabou
     * @param p0 : solution courante
     * @param neighbor : voisin candidat
     * @param iter : iteration courante
     * @param tabuTenure : durée d'interdiction
     * @return : if mouvement est tabou
     */
    private boolean isTabuNeighbor(int[] p0, FitnessObject neighbor, int iter, int tabuTenure){
        int[] p0_tmp = Utilities.permute(p0, neighbor.getSequence());
        int[] positions = Utilities.string_to_intArray(neighbor.getSequence());
        int position_i = positions[0]-1;
        int position_j = positions[1]-1;
        int objet_i = p0_tmp[position_i];
        int objet_j = p0_tmp[position_j];
        if(tabu[objet_i-1][position_i] > iter && tabu[objet_j-1][position_j] > iter){
            return true;
        }else{
            tabu[p0[position_i]-1][position_i] = iter+tabuTenure+1;
            tabu[p0[position_j]-1][position_j] = iter+tabuTenure+1;
        }
        return false;
    }

    /**
     * On initialise nos structures de donnés
     * N, Matrix_D, Matrix_W, Tabu List
     *
     */
    public void initialisation(String filename){
        ReadFile readFile = new ReadFile(filename);
        readFile.read_data();
        this.N = readFile.getN();
        this.D = readFile.getMatrix_D();
        this.W = readFile.getMatrix_W();
        this.diversificationMatrix = new int[N][N];
        u_iter = (int)Math.pow(N, 2);
    }

    /***
     * Programme principale
     * @param args
     */
    public static void main(String[] args) {
        String path_filename = "src"+File.separator+"data"+File.separator+"1.dat";
        TabuSearch tabou = new TabuSearch(path_filename);
//        TabuSearch tabou = new TabuSearch(args[0]);
    }
}
