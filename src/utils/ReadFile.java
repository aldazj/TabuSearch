package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class ReadFile {
	
	private BufferedReader br;
	private File file;
	private String path_filename; 
	private int N;							//nb objets
	private int[][] matrix_D;				//matrice de distances
	private int[][] matrix_W;				//matrice des flux
	
	
	public ReadFile(String path_filename) {
		this.path_filename = path_filename;
	}
	
	public void read_data(){
		file = new File(path_filename);
		String line = "";
		try {
			br = new BufferedReader(new FileReader(file));
			boolean get_value_N = false;
			boolean get_matrix_D = false;
			boolean get_matrix_W = false;
			int data_element = 1;
			int index_line = 0;
			while ((line = br.readLine()) != null) {
				if(!lineEmpty(line) && !lineComment(line)){
					if(data_element == 1){
						N = Integer.parseInt(line);
						matrix_D = new int[N][N];
						matrix_W = new int[N][N];
						init_matrix(matrix_D);
						init_matrix(matrix_W);
						get_value_N = true;
					}else if(data_element == 2){
						String []tmp_line = line.replace("  ", " ").split(" ");	
						for (int i = 0; i < tmp_line.length; i++) {
							matrix_D[index_line][i] = Integer.parseInt(tmp_line[i]);
						}
						if(index_line == N-1){
							get_matrix_D = true;
							index_line = -1;
						}
						index_line++;
					}else if(data_element == 3){
						String []tmp_line = line.replace("  ", " ").split(" ");	
						for (int i = 0; i < tmp_line.length; i++) {
							matrix_W[index_line][i] = Integer.parseInt(tmp_line[i].trim());
						}
						index_line++;
					}	
					if(get_value_N){
						data_element++;
						get_value_N = false;
					}else if(get_matrix_D){
						data_element++;
						get_matrix_D = false;
					}else if(get_matrix_W){
						data_element++;
						get_matrix_W = false;
					}
				}
			}
			br.close();			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean lineEmpty(String line){
		return line.isEmpty();
	}
	
	private boolean lineComment(String line){
		return line.startsWith("#") || line.startsWith("!") || line.startsWith(";"); 
	}
	
	private void init_matrix(int[][] matrix){
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = 0;
			}
		}
	}
	
 	public int getN() {
		return N;
	}

	public int[][] getMatrix_W() {
		return matrix_W;
	}

	public int[][] getMatrix_D() {
		return matrix_D;
	}

}
