import java.util.Scanner;
import java.lang.Math;
import java.util.ArrayList;

class MineSweeper
{
	public final String EMPTY_CELL = " ";		// non number cells definiton in table
	public final String FLAGGED_CELL = "F";
	public final String MINE_CELL = "X";


	private int xLength, yLength;			// x, y length of the table
	private int[][] mineSweeper;			// real table
	private String[][] shownMineSweeper;	// atble user interacts with
	private String difficulty = "M";    	// difficulty level
	private int diffPercent = 50;
	private int mineCount = 0;
	private String isWin = "continues";		// continues, win, loss


	public static void main(String[] args)
	{
		new MineSweeper();	
	}

	public MineSweeper()
	{

		Scanner scan = new Scanner(System.in);

		System.out.println(":=:=:=:=:  WELCOME  :=:=:=:=:");
		System.out.println("This is a minesweeper game.\n");

		String input;
		while (true)
		{
			System.out.print("Please enter the sizes of the board (m x n): ");
			input = scan.nextLine();

			xLength = Integer.parseInt(input.substring(0, input.indexOf(" ")));
			yLength = Integer.parseInt(input.substring(input.lastIndexOf(" ") + 1));
			if (xLength > 1 & yLength > 1)
				break;
			else
				System.out.println("Height or width cannot be 1 or less. Please try again.");
		}

		System.out.print("Please select the difficulty (E, M, H): ");
		difficulty = scan.nextLine();
		System.out.println();

		if (difficulty.equals("E"))
			diffPercent = 15;
		else if (difficulty.equals("M"))
			diffPercent = 25;
		else if (difficulty.equals("H"))
			diffPercent = 40;
		else 
			diffPercent = Integer.parseInt(difficulty);		

		
		mineCount = xLength * yLength * diffPercent / 100 ;	// this is the formula for how many mines we will put

		createTable();

		while (isWin.equals("continues"))
		{
			printTable();

			while (true)		// it works till it gets a working input, after that it makes the move
			{
				input = "";
				System.out.print("Please make a move (x, y): ");
				input = scan.nextLine();
				System.out.println();
				String moveType = "";
		
				if (input.contains("F"))
				{
					moveType = "F";
					input = input.substring(0, input.lastIndexOf(" "));
				}
				if (input.contains("U"))
				{
					moveType = "U";
					input = input.substring(0, input.lastIndexOf(" "));
				}


				int xInput, yInput;				// coordinates of the next move
				xInput = Integer.parseInt(input.substring(0, input.indexOf(",")));
				yInput = Integer.parseInt(input.substring(input.indexOf(" ") + 1, input.length()));	
				yInput = yLength - yInput;		// we reversed the input to matrix's shape
				xInput -= 1;					// we adjusted the input to matrix's shape
		
				
				
				String errorCode = "";
				if (moveType.equals("F"))
				{
					errorCode = putFlag(xInput, yInput);
					if (errorCode.equals("already opened"))
						System.out.println("Open cells cannot be flagged.\n");
					if (errorCode.equals("already flagged"))
						System.out.println("The cell is already flagged.\n");
					if (errorCode.equals(""))
						break;									// this move is acceptable
				}
				if (moveType.equals("U"))
				{
					errorCode = removeFlag(xInput, yInput);
					if (errorCode.equals("not flagged"))
						System.out.println("Only flagged cells can be unflagged.\n");
					if (errorCode.equals(""))
						break;									// this move is acceptable
				}
				if (moveType.equals(""))
				{
					errorCode = openCell(xInput, yInput);
					if (errorCode.equals("already flagged"))
						System.out.println("Flagged cells cannot be opened.\n");
					if (errorCode.equals("already opened"))
						System.out.println("This cell is already opened\n");
					if (errorCode.equals(""))
						break;									// this move is acceptable
				}
			}

			boolean win = true;
			for (int a = 0; a < yLength; a++)
			{
				for (int b = 0; b < xLength; b++)
				{
					if (mineSweeper[a][b] == -1) 								// bombs cells
					{
						if (!(shownMineSweeper[a][b].equals(FLAGGED_CELL) | shownMineSweeper[a][b].equals(EMPTY_CELL)))		// needs to be flagged
							win = false;
					}
					else	
					{															// non-bomb cells
						if (shownMineSweeper[a][b].equals(EMPTY_CELL))			// needs to be not empty
							win = false;
						if (shownMineSweeper[a][b].equals(FLAGGED_CELL))		// needs to be not flagged
							win = false;
					}
				}
			}
			if (win)
			{
				isWin = "win";
				break;
			}
		}

		printTable();

		if (isWin.equals("loss"))
			System.out.println("You lost, better luck next time.\n");
		if (isWin.equals("win"))
			System.out.println("Congratulations, you won.\n");
		
		scan.close();	
	}

	// error messages:
	// ""
	// "alrdeady flagged"
	// "already opened"
	private String putFlag(int x, int y)	
	{
		if (shownMineSweeper[y][x].equals("F"))
			return "already flagged";
		else if (shownMineSweeper[y][x].equals(EMPTY_CELL))
		{
			shownMineSweeper[y][x] = "F";
			return "";
		}
		else 
			return "already opened";
	}

	// ""
	// "not flagged"
	private String removeFlag(int x, int y)
	{
		if (shownMineSweeper[y][x].equals("F"))
		{
			shownMineSweeper[y][x] = EMPTY_CELL;
			return "";
		}
		else 
			return "not flagged";
	}

	// ""
	// "already flagged"
	// "already opened"
	private String openCell(int x, int y)
	{
		if (shownMineSweeper[y][x].equals("F"))
			return "already flagged";
		else if (!shownMineSweeper[y][x].equals(EMPTY_CELL))
			return "already opened";
		else
		{
			if (mineSweeper[y][x] == -1)		// run bomb func
				openMine(x, y);
			else if (mineSweeper[y][x] == 0)	// run zero func
				openZero(x, y);
			else
				openNonZeroNonMine(x, y);		// run other func
			return "";
		}
	}

	private void openNonZeroNonMine(int x, int y)
	{
		shownMineSweeper[y][x] = "" + mineSweeper[y][x];
	}

	private void openMine(int x, int y)
	{
		isWin = "loss";

		for (int a = 0; a < yLength; a++)
		{
			for (int b = 0; b < xLength; b++)
				shownMineSweeper[a][b] = (mineSweeper[a][b] == -1) ? MINE_CELL : "" + mineSweeper[a][b];
				// shows all the board and changes -1's with "X"
		}
	}

	private void openZero(int x, int y)
	{
		shownMineSweeper[y][x] = "" + mineSweeper[y][x];		// önce ilk kareyi açtık

		ArrayList<int[]> neighbours = new ArrayList<int[]>();
		int[][] neighboursTemp = checkNeighbours(x, y);
		for (int a = 0; a < neighboursTemp[0].length; a++)		// ilk karenin komşularını bul
		{
			int[] currentNeighbour = {neighboursTemp[0][a], neighboursTemp[1][a], neighboursTemp[2][a]};
			neighbours.add(currentNeighbour);					// ilk karenin komşuları neighbours'a eklendi
		}

		for (int a = 0; a < neighbours.size() ; a++)			// neighbours listeninin tüm elemanlarını açtık, 
		{														// yeni 0 bulursak neighbour'larını listenin en sonuna ekledi
			shownMineSweeper[neighbours.get(a)[2]][neighbours.get(a)[1]] = "" + mineSweeper[neighbours.get(a)[2]][neighbours.get(a)[1]];
			if (neighbours.get(a)[0] == 0)
			{
				int[][] neighboursTemp2 = checkNeighbours(neighbours.get(a)[1], neighbours.get(a)[2]);		// her bir komşunun komşularını bul

				for (int b = 0; b < neighboursTemp2[0].length; b++)
				{
					int[] currentNeighbour = {neighboursTemp2[0][b], neighboursTemp2[1][b], neighboursTemp2[2][b]};
					boolean isSame = false;
					for (int d = 0; d < neighbours.size(); d++)
						if (neighbours.get(d)[1] == currentNeighbour[1] & neighbours.get(d)[2] == currentNeighbour[2]) //x&y are equal to x&y -> same element exists
							isSame = true;

					if (isSame == false)																		// daha önce eklenmediyse
						neighbours.add(currentNeighbour);
			
				}
			}
		}
	}

	private void createTable()
	{

		mineSweeper      = new int[yLength][xLength];
		shownMineSweeper = new String[yLength][xLength];

	
		for (int a = 0; a < mineCount; a++)				// put mines exactly mineCount times in random places
		{
			while (true)	// if it chooses a mine it will try again
			{
				int yRan = randomInt(yLength);
				int xRan = randomInt(xLength);
				if (mineSweeper[yRan][xRan] != -1)
				{
					mineSweeper[yRan][xRan] = -1;
					break;
				}
			}
		}


		for (int a = 0; a < yLength; a++)				//We put how many bombs is someone neighbour to.
		{
			for (int b = 0; b < xLength; b++)
			{
				if (mineSweeper[a][b] == 0)
					mineSweeper[a][b] = findRepeats(-1, checkNeighbours(b, a)[0]);
			} 
		}

		for (int a = 0; a < yLength; a++)				// At first the shownMineSweeper is empty
		{
			for (int b = 0; b < xLength; b++)
			{
				shownMineSweeper[a][b] = EMPTY_CELL;
			} 
		}
	}



	private int[][] checkNeighbours(int x, int y)		// return the cells neighbours stats 
	{													// 0-> how many bomb neighbours
														// 1,2-> x,y coordinates
		int[][] neighbours;
		if ((x == 0) & (y == 0))											// corners
		{
			neighbours = new int[3][3];
			neighbours[0][0] = mineSweeper[y][x + 1];
			neighbours[0][1] = mineSweeper[y + 1][x];
			neighbours[0][2] = mineSweeper[y + 1][x + 1];

			neighbours[1][0] = x + 1;
			neighbours[1][1] = x;
			neighbours[1][2] = x + 1;

			neighbours[2][0] = y;
			neighbours[2][1] = y + 1;
			neighbours[2][2] = y + 1;

		}
		else if ((x == xLength - 1) & (y == 0))
		{
			neighbours = new int[3][3];
			neighbours[0][0] = mineSweeper[y][x - 1];
			neighbours[0][1] = mineSweeper[y + 1][x];
			neighbours[0][2] = mineSweeper[y + 1][x - 1];

			neighbours[1][0] =  x - 1;
			neighbours[1][1] =  x;
			neighbours[1][2] =  x - 1;

			neighbours[2][0] = y;
			neighbours[2][1] = y + 1;
			neighbours[2][2] = y + 1;
		}
		else if ((x == 0) & (y == yLength - 1))
		{
			neighbours = new int[3][3];
			neighbours[0][0] = mineSweeper[y - 1][x];
			neighbours[0][1] = mineSweeper[y - 1][x + 1];
			neighbours[0][2] = mineSweeper[y][x + 1];

			neighbours[1][0] = x;
			neighbours[1][1] = x + 1;
			neighbours[1][2] = x + 1;

			neighbours[2][0] = y - 1;
			neighbours[2][1] = y - 1;
			neighbours[2][2] = y;
		}
		else if ((x == xLength - 1) & (y == yLength - 1))
		{
			neighbours = new int[3][3];
			neighbours[0][0] = mineSweeper[y - 1][x - 1];
			neighbours[0][1] = mineSweeper[y - 1][x];
			neighbours[0][2] = mineSweeper[y][x - 1];

			neighbours[1][0] = x - 1;
			neighbours[1][1] = x;
			neighbours[1][2] = x - 1;
			
			neighbours[2][0] = y - 1;
			neighbours[2][1] = y - 1;
			neighbours[2][2] = y;
		}
		else if (x == xLength - 1)											// edges
		{
			neighbours = new int[3][5];
			neighbours[0][0] = mineSweeper[y - 1][x - 1];
			neighbours[0][1] = mineSweeper[y - 1][x];
			neighbours[0][2] = mineSweeper[y][x - 1];
			neighbours[0][3] = mineSweeper[y + 1][x - 1];
			neighbours[0][4] = mineSweeper[y + 1][x];

			neighbours[1][0] = x - 1;
			neighbours[1][1] = x;
			neighbours[1][2] = x - 1;
			neighbours[1][3] = x - 1;
			neighbours[1][4] = x;
			
			neighbours[2][0] = y - 1;
			neighbours[2][1] = y - 1;
			neighbours[2][2] = y;
			neighbours[2][3] = y + 1;
			neighbours[2][4] = y + 1;
		}
		else if (x == 0)
		{
			neighbours = new int[3][5];
			neighbours[0][0] = mineSweeper[y - 1][x];
			neighbours[0][1] = mineSweeper[y - 1][x + 1];
			neighbours[0][2] = mineSweeper[y][x + 1];
			neighbours[0][3] = mineSweeper[y + 1][x];
			neighbours[0][4] = mineSweeper[y + 1][x + 1];

			neighbours[1][0] = x;
			neighbours[1][1] = x + 1;
			neighbours[1][2] = x + 1;
			neighbours[1][3] = x;
			neighbours[1][4] = x + 1;

			neighbours[2][0] = y - 1;
			neighbours[2][1] = y - 1;
			neighbours[2][2] = y;
			neighbours[2][3] = y + 1;
			neighbours[2][4] = y + 1;
		}
		else if (y == yLength - 1)
		{
			neighbours = new int[3][5];
			neighbours[0][0] = mineSweeper[y - 1][x - 1];
			neighbours[0][1] = mineSweeper[y - 1][x];
			neighbours[0][2] = mineSweeper[y - 1][x + 1];
			neighbours[0][3] = mineSweeper[y][x - 1];
			neighbours[0][4] = mineSweeper[y][x + 1];

			neighbours[1][0] = x - 1;
			neighbours[1][1] = x;
			neighbours[1][2] = x + 1;
			neighbours[1][3] = x - 1;
			neighbours[1][4] = x + 1;

			neighbours[2][0] = y - 1;
			neighbours[2][1] = y - 1;
			neighbours[2][2] = y - 1;
			neighbours[2][3] = y;
			neighbours[2][4] = y;
		}
		else if (y == 0)
		{
			neighbours = new int[3][5];
			neighbours[0][0] = mineSweeper[y][x - 1];
			neighbours[0][1] = mineSweeper[y][x + 1];
			neighbours[0][2] = mineSweeper[y + 1][x - 1];
			neighbours[0][3] = mineSweeper[y + 1][x];
			neighbours[0][4] = mineSweeper[y + 1][x + 1];

			neighbours[1][0] = x - 1;
			neighbours[1][1] = x + 1;
			neighbours[1][2] = x - 1;
			neighbours[1][3] = x;
			neighbours[1][4] = x + 1;

			neighbours[2][0] = y;
			neighbours[2][1] = y;
			neighbours[2][2] = y + 1;
			neighbours[2][3] = y + 1;
			neighbours[2][4] = y + 1;
		}
		else 																// other points
		{
			neighbours = new int[3][8];
			neighbours[0][0] = mineSweeper[y - 1][x - 1];
			neighbours[0][1] = mineSweeper[y - 1][x];
			neighbours[0][2] = mineSweeper[y - 1][x + 1];
			neighbours[0][3] = mineSweeper[y][x - 1];
			neighbours[0][4] = mineSweeper[y][x + 1];
			neighbours[0][5] = mineSweeper[y + 1][x - 1];
			neighbours[0][6] = mineSweeper[y + 1][x];
			neighbours[0][7] = mineSweeper[y + 1][x + 1];

			neighbours[1][0] = x - 1;
			neighbours[1][1] = x;
			neighbours[1][2] = x + 1;
			neighbours[1][3] = x - 1;
			neighbours[1][4] = x + 1;
			neighbours[1][5] = x - 1;
			neighbours[1][6] = x;
			neighbours[1][7] = x + 1;

			neighbours[2][0] = y - 1;
			neighbours[2][1] = y - 1;
			neighbours[2][2] = y - 1;
			neighbours[2][3] = y;
			neighbours[2][4] = y;
			neighbours[2][5] = y + 1;
			neighbours[2][6] = y + 1;
			neighbours[2][7] = y + 1;
		}

		return neighbours;
	}
	
	public void printTable()						// prints the table
	{

		for (int a = 0; a < yLength; a++)			// for all rows
		{
			System.out.print("      ");
			for (int b = 0; b < xLength - 1; b++)				// before row
				System.out.print("|   ");

				System.out.println();

			if (yLength - a < 10)
				System.out.print(yLength - a + "  ");
			else if (yLength - a < 100)
				System.out.print(yLength - a + " ");
			else if (yLength - a < 1000)
				System.out.print(yLength - a + "");

			for (int b = 0; b < xLength; b++)					// the row which has the data
			{
				if (b != 0)
					System.out.print("|");
				
				System.out.print(" " + shownMineSweeper[a][b] + " ");
			}
			System.out.println();

			if (a == yLength - 1)								// after row
			{
				System.out.print("      ");
				for (int b = 0; b < xLength - 1; b++)
					System.out.print("|   ");

				System.out.println();
			}
			else
			{
				System.out.print("   ___");
				for (int b = 0; b < xLength - 1; b++)
					System.out.print("|___");

				System.out.println();
			}
		}

		System.out.print("   ");
		for (int b = 0; b < xLength; b++)
		{
			if (b < 9)
				System.out.print(" " + (b + 1) + "  ");
			else
				System.out.print(" " + (b + 1) % 10 + "  ");

		}
		System.out.println();
		System.out.println();
	}

	public int findRepeats(int arg, int[] list)
	{
		int repeat = 0;
		for (int element: list)
		{
			if (element == arg)
				repeat+= 1;
		}
		return repeat;
	}

	public int randomInt(int excludes)
	{
		double ran = Math.random();
		return (int) (ran * excludes);
	}
}