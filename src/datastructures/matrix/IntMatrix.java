package datastructures.matrix;
class IntMatrix {
	private double[][] data;
	private int rowCount;
	private int columnCount;
	public IntMatrix(int rowCount, int columnCount) {
		this.data = new double[rowCount][columnCount];
		this.rowCount = rowCount;
		this.columnCount = columnCount;
	}
	public int getRowCount() {
		return this.rowCount;
	}
	public int getColumnCount() {
		return this.columnCount;
	}
	public double getValue(int row, int column) {
		return data[row][column];
	}
	public void setValue(int row, int column, double value) {
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
	public IntMatrix multiply(IntMatrix matrix2) {
		if (this.columnCount != matrix2.rowCount) {
			throw new IllegalArgumentException("Matrices cannot be multiplied");
		}
		IntMatrix result = new IntMatrix(this.rowCount, matrix2.columnCount);
		for (int i = 0; i < result.rowCount; i++) {
			for (int j = 0; j < result.columnCount; j++) {
				double sum = 0;
				for (int k = 0; k < this.columnCount; k++) {
					sum += this.data[i][k] * matrix2.data[k][j];
				}
				result.data[i][j] = sum;
			}
		}
		return result;
	}
	public IntMatrix dotProduct(IntMatrix matrix2) {
		if (this.columnCount != matrix2.rowCount) {
			throw new IllegalArgumentException(
					"The number of columns in the first matrix must be the same as the number of rows in the second matrix");
		}
		IntMatrix product = new IntMatrix(this.rowCount, matrix2.columnCount);
		for (int i = 0; i < rowCount; i++) {
			for (int j = 0; j < matrix2.columnCount; j++) {
				double sum = 0;
				for (int k = 0; k < matrix2.rowCount; k++) {
					sum += this.data[i][k] * matrix2.data[k][j];
				}
				product.setValue(i, j, sum);
			}
		}
		return product;
	}
	public IntMatrix add(IntMatrix matrix2) {
		IntMatrix result = new IntMatrix(this.rowCount, this.columnCount);
		for (int i = 0; i < this.rowCount; i++) {
			for (int j = 0; j < this.columnCount; j++) {
				result.data[i][j] = this.data[i][j] + matrix2.data[i][j];
			}
		}
		return result;
	}
	public IntMatrix subtract(IntMatrix matrix2) {
		IntMatrix result = new IntMatrix(this.rowCount, this.columnCount);
		for (int i = 0; i < this.rowCount; i++) {
			for (int j = 0; j < this.columnCount; j++) {
				result.data[i][j] = this.data[i][j] - matrix2.data[i][j];
			}
		}
		return result;
	}
	public IntMatrix transpose() { 
		IntMatrix result = new IntMatrix(columnCount, rowCount);
	    for (int i=0; i < rowCount; i++) {
	        for (int j=0; j < columnCount; j++) {
	            result.data[j][i] = this.data[i][j];
	        }
	    }
	    return result;
    }
}
