import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import java.lang.Math.*;

//---------The Board class is responsible for running of the whole game-----//

class Board extends JPanel implements KeyListener, Runnable{
	//BlockArray is for contained all the blocks that are on the board. The original point is the left bottom.
	private Block[][] blockArray=new Block[11][4];
	//rowArray contains the information of how many blocks have been on the board in every row.
	private int[] rowArray={0,0,0,0};
	private Block block;
	private Color[] colorList={new Color(200,179,221),new Color(154,108,201),new Color(112,56,170),new Color(67,9,127),new Color(30,1,59)};
	//isStart controls whether it's paused. isFast controls whether it's falling fast. isReset controls whether it has been reseted.
	private boolean isStart=true;
	private boolean isFast=false;
	private boolean isReset=false;
	private int score;
	JTextField scoreField=new JTextField();
	JOptionPane optionpane=new JOptionPane();

	//Initialize the board and set layout.
	public Board(){
		setLayout(null);
		setSize(400,520);
		score=0;
		scoreField.setBounds(0,0,100,20);
		scoreField.setEnabled(false);
		scoreField.setText("0");
		scoreField.setHorizontalAlignment(JTextField.CENTER);
		add(scoreField);
	}

	public int getScore(){
		return score;
	}
	public void run(){
		addKeyListener(this);
		while (true){

			//Every time choose a random xAxis and a random color and create a new block.
			Random random=new Random();
			int x=Math.abs(random.nextInt())%4;
			int colorIndex=Math.abs(random.nextInt())%4;
			int y=0;

			//If the last block falled has a yAxis smaller than 50, no more block can fall on that, then an option panel pops up and shows game over.
			if (rowArray[x]!=0 && blockArray[rowArray[x]-1][x].getY()<50){
				optionpane.showMessageDialog(null, "Game Over", "Oops!", JOptionPane.ERROR_MESSAGE);
				break;
			}
			block=new Block("");
			block.setParameter(x,y,100,50);
			block.setColorByObject(colorList[colorIndex]);
			block.exhibit();

			//After a creation of new block, refocus the key listener to the board.
			this.requestFocus();

			//Control the falling of block. Each time sleep the thread for 5 milliseconds to create falling animation.
			while (y<470){

				// if already reseted, exit this falling loop.
				if (isReset) {
					break;
				}
				block.place(y);

				// if already paused, stop letting the block falling.
				if (isStart) {
					y++;
				}

				//Stop falling down if it reaches the last block on this row.
				if(rowArray[block.getX()]!=0){
					if (y+49==blockArray[rowArray[block.getX()]-1][block.getX()].getY()) break;
				}

				//Sleep the thread to get the falling animation
				try {
					if (isFast) Thread.sleep(1);
					else Thread.sleep(5);
		        } catch (InterruptedException e) {
		            // TODO Auto-generated catch block
		            e.printStackTrace();
		        }
			}

			//When a block falls to the right location, set isFast to false to let the next one fall in the normal speed
			isFast=false;
			if (isReset) {
				isReset=false;
				continue;
			}

			//Put newly falled block to the corresponding position in the blockArray.
			blockArray[rowArray[block.getX()]][block.getX()]=block;
			rowArray[block.getX()]+=1;

			//If there are at least two blocks in this row and the first two have the same color, merge them.
			while (rowArray[block.getX()]-2>=0){
				if (!blockArray[rowArray[block.getX()]-1][block.getX()].getColor().toString().equals(colorList[4].toString())){
					if (blockArray[rowArray[block.getX()]-2][block.getX()].getColor().toString().equals(blockArray[rowArray[block.getX()]-1][block.getX()].getColor().toString())){
						merge(blockArray[rowArray[block.getX()]-1][block.getX()],blockArray[rowArray[block.getX()]-2][block.getX()]);
						score+=6; // Each merge adds 6 scores.
					}
					else break;
				}
				else break;
			}

			//Check the highest column with full four blocks.
			int full=10000;
			for (int k=0;k<4;k++){
				if (full>rowArray[k]) full=rowArray[k];
			}

			//From the bottom column to the full column, check whether four blocks have the same color.
			//If so, eliminate that line.
			int l=0;
			while (l<full){
				Block block1=blockArray[l][0];
				Block block2=blockArray[l][1];
				Block block3=blockArray[l][2];
				Block block4=blockArray[l][3];
				if (block1.getColor().toString().equals(block2.getColor().toString()) && block2.getColor().toString().equals(block3.getColor().toString()) && block3.getColor().toString().equals(block4.getColor().toString())){
					decreaseOneLine(l);

					//If one line is eliminated, keep l the same and decrease full by 1, because the upper lines have go down.
					full--;
					score+=100; //Each line elimination add 100 scores.
				}
				else l++;
			}
			score+=2; // Each block falling adds 2 scores.
			//display the updated score.
			scoreField.setText(Integer.toString(score));
		}
	}

	//------Merge Algorithm--------//

	public void merge(Block blockUp, Block blockDown){
		//Find the before-merge color in the colorList
		Color previousColor=blockUp.getColor();
		int previousColorIndex=0;
		for (int k=0;k<5;k++){
			if (colorList[k].toString().equals(previousColor.toString())){
				previousColorIndex=k;
				break;
			}
		}
		//The darkest color cannot merge
		if (previousColorIndex!=4){

			//Set both block to the new color
			blockUp.setColorByObject(colorList[previousColorIndex+1]);
			blockDown.setColorByObject(colorList[previousColorIndex+1]);
			//Make the up block fall down to the right position.
			int yAdd=0;
			while (yAdd<50){
				blockUp.place(blockUp.getY()+yAdd);
				yAdd++;
				if (blockUp.getY()+49==blockArray[rowArray[blockUp.getX()]-1][blockUp.getX()].getY()) break;
				try {
					Thread.sleep(15);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//Make the upblock disappear in the board and boardArray because the downblock is already the updated color.
			blockUp.disappear();
			blockArray[rowArray[blockUp.getX()]-1][blockUp.getX()]=null;
			//rowArray has one less blocks in this row.
			rowArray[blockUp.getX()]--;
		}
	}

	//------ Elimination Algorithm ------//

	public void decreaseOneLine(int line){
		Color color;
		//For each row, change each block color to its upper block's.
		for (int i=0;i<4;i++){
			for (int k=line; k<rowArray[i]-1;k++){
				color=blockArray[k+1][i].getColor();
				blockArray[k][i].setColorByObject(color);
			}
			//Throws the very upper block to nowhere and set it to null in the blockArray (rowArray should decrese by 1 correspondently)
			blockArray[rowArray[i]-1][i].place(100000);
			blockArray[rowArray[i]-1][i]=null;
			rowArray[i]-=1;
		}
	}

	public void keyTyped(KeyEvent e) {}
	public void keyPressed(KeyEvent e){
		if (e.getKeyCode()==KeyEvent.VK_RIGHT){
			//If the block is not at the very right side and it doesn't have another block on its right, press right key to move it to the right one block.
			if (block.getX()<3){
				if (rowArray[block.getX()+1]!=0){
					if (block.getY()>blockArray[rowArray[block.getX()+1]-1][block.getX()+1].getY()-50){
						return;
					}
					else block.rightMove(1);
				}
				else block.rightMove(1);
			}
		}
		if (e.getKeyCode()==KeyEvent.VK_LEFT){
			//If the block is not at the very left side and it doesn't have another block on its left, press left key to move it to the left one block.
			if (block.getX()>0){
				if (rowArray[block.getX()-1]!=0){
					if (block.getY()>blockArray[rowArray[block.getX()-1]-1][block.getX()-1].getY()-50){
						return;
					}
					else block.leftMove(1);
				}
				else block.leftMove(1);
			}
		}
		if (e.getKeyCode()==KeyEvent.VK_DOWN){
			//Press down to turn on the fast mode for each block.
			//It will change back to false after one block finishes falling.
			if (!isFast) isFast=true;
		}
		if (e.getKeyCode()==KeyEvent.VK_SPACE){
			//Press space to change whether the board should be paused or not.
			if (isStart) isStart=false;
			else isStart=true;
		}
		if (e.getKeyCode()==KeyEvent.VK_R){
			//Press r to reset
			isReset=true;
			//Clear the score.
			scoreField.setText("0");
			score=0;
			//Throw the falling block out
			block.place(100000);
			//Throw all the existing block out and clear the blockArray and rowArray.
			for (int i=0;i<11;i++){
				for (int j=0;j<4;j++){
					if (blockArray[i][j]!=null){
						blockArray[i][j].place(1000000);
						blockArray[i][j]=null;
					}
				}
			}
			for (int k=0;k<4;k++){
				rowArray[k]=0;
			}
		}
	}
	public void keyReleased(KeyEvent e) {}

	//Block class has a label in certain size inside to support the rectangle blocks.
	//The methods are useful but can be easily understood by name.
	class Block{
		private int x,y,blockWidth,blockHeight;
		private Color color;
		private JLabel label;
		public Block(String string){
			label=new JLabel(string);
			this.x=0;
			this.y=0;
		}
		//Set the rectangle block
		public void setParameter(int x,int y,int blockWidth,int blockHeight){
			this.x=x;
			this.y=y;
			this.blockWidth=blockWidth;
			this.blockHeight=blockHeight;
			label.setBounds(x*blockWidth,y,blockWidth,blockHeight);
		}
		public void setColorByObject(Color c){
			this.color=c;
			label.setOpaque(true);
			label.setBackground(this.color);
		}
		public void setColorByRGB(int r, int g, int b){
			this.color=new Color(r,g,b);
			label.setOpaque(true);
			label.setBackground(this.color);
		}
		public Color getColor(){
			return color;
		}
		public void place(int y){
			this.y=y;
			label.setBounds(this.x*this.blockWidth,this.y,this.blockWidth,this.blockHeight);
		}
		public void exhibit(){
			add(label);
		}
		public void disappear(){
			label.setEnabled(false);
		}
		public void leftMove(int x){
			if (0<this.x && this.y<470){
				this.x-=1;
				label.setBounds(this.x*this.blockWidth,this.y,this.blockWidth,this.blockHeight);
			}
		}
		public void rightMove(int x){
			if (3-this.x>0 && this.y<470){
				this.x+=1;
				label.setBounds(this.x*this.blockWidth,this.y,this.blockWidth,this.blockHeight);
			}
		}
		public int getX(){
			return this.x;
		}
		public int getY(){
			return this.y;
		}
	}
}

//This is the main frame of the game which extends a JFrame.
public class Shades extends JFrame{
	private JPanel menu;
	private Board board;
	private JTextField score;
	Thread t;
	public Shades(){
		int scoreNum=0;
		setLayout(new BorderLayout());
		JPanel menu=new JPanel();
		menu.setLayout(new BorderLayout());
		menu.add(new JLabel("Level 1"), BorderLayout.WEST);
		//Use button object only for making it more nice in layout
		menu.add(new JButton("Press Space to Pause."),BorderLayout.CENTER);
		menu.add(new JButton("Press R to reset"),BorderLayout.EAST);
		add(menu,BorderLayout.NORTH);
		board=new Board();
		//start the thread of the board
		t=new Thread(board);
		t.start();
		add(board, BorderLayout.CENTER);
	}

	public static void main(String[] args){
		Shades frame=new Shades();
		frame.setTitle("Shades game");
		frame.setSize(400,570);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}