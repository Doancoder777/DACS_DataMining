package datastructures.matrix;
class DoubleMatrix {
	private int[][] data;
	private int rowCount;
	private int columnCount;
	public DoubleMatrix(int rowCount, int columnCount) {
		this.data = new int[rowCount][columnCount];
		this.rowCount = rowCount;
		this.columnCount = columnCount;
	}
	public int getRowCount() {
		return this.rowCount;
	}
	public int getColumnCount() {
		return this.columnCount;
	}
	public int getValue(int row, int column) {
		return data[row][column];
	}
	public void setValue(int row, int column, int value) {
		data[row][column] = value;
	}
	public void printMatrix() {
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				System.out.print(data[i][j] + "\t");
			}
			System.out.println();
		}
	}
	public DoubleMatrix multiply(DoubleMatrix matrix2) {
		if (this.columnCount != matrix2.rowCount) {
			throw new IllegalArgumentException("Matrices cannot be multiplied");
		}
		DoubleMatrix result = new DoubleMatrix(this.rowCount, matrix2.columnCount);
		for (int i = 0; i < result.rowCount; i++) {
			for (int j = 0; j < result.columnCount; j++) {
				int sum = 0;
				for (int k = 0; k < this.columnCount; k++) {
					sum += this.data[i][k] * matrix2.data[k][j];
				}
				result.data[i][j] = sum;
			}
		}
		return result;
	}
	public DoubleMatrix dotProduct(DoubleMatrix matrix2) {
		if (this.columnCount != matrix2.rowCount) {
			throw new IllegalArgumentException(
					"The number of columns in the first matrix must be the same as the number of rows in the second matrix");
		}
		DoubleMatrix product = new DoubleMatrix(this.rowCount, matrix2.columnCount);
		for (int i = 0; i < rowCount; i++) {
			for (int j = 0; j < matrix2.columnCount; j++) {
				int sum = 0;
				for (int k = 0; k < matrix2.rowCount; k++) {
					sum += this.data[i][k] * matrix2.data[k][j];
				}
				product.setValue(i, j, sum);
			}
		}
		return product;
	}
	public DoubleMatrix add(DoubleMatrix matrix2) {
		DoubleMatrix result = new DoubleMatrix(this.rowCount, this.columnCount);
		for (int i = 0; i < this.rowCount; i++) {
			for (int j = 0; j < this.columnCount; j++) {
				result.data[i][j] = this.data[i][j] + matrix2.data[i][j];
			}
		}
		return result;
	}
	public DoubleMatrix subtract(DoubleMatrix matrix2) {
		DoubleMatrix result = new DoubleMatrix(this.rowCount, this.columnCount);
		for (int i = 0; i < this.rowCount; i++) {
			for (int j = 0; j < this.columnCount; j++) {
				result.data[i][j] = this.data[i][j] - matrix2.data[i][j];
			}
		}
		return result;
	}
	public DoubleMatrix transpose() { 
		DoubleMatrix result = new DoubleMatrix(columnCount, rowCount);
	    for (int i=0; i < rowCount; i++) {
	        for (int j=0; j < columnCount; j++) {
	            result.data[j][i] = this.data[i][j];
	        }
	    }
	    return result;
    }
}
