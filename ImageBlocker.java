import java.io.*;
import java.util.*;
import javax.vecmath.*;

import javax.imageio.*;
import java.awt.image.*;
import java.awt.*;

/**
 * Converts an image into Block objects, and identifies connected
 * components to generate "RigidBody" objects.  
 * 
 * NOTE: YOU DON'T HAVE TO (or want to!) MODIFY THIS CODE.
 * 
 * @author Doug James, March 2007.
 */
public class ImageBlocker 
{
	/** Image in question. */
	private BufferedImage image;

	/** #Rows */
	private int M;
	/** #Columns */
	private int N;

	/** Block[i][j] with null for massless/background Blocks. */
	private Block[][] B;

	/** Resulting sets of connected Block components. */
	private ArrayList<HashSet<Block>> components = new ArrayList<HashSet<Block>>();

	/**
	 *
	 *
	 * @param tgaFilename TGA image filename (RGB, uncompressed)
	 * containing rigid image elements on a white background.
	 */
	public ImageBlocker(String fileName) throws IOException
	{
		image = loadImage(fileName);

		buildBlocks();

		findConnectedComponents();
	}

	/** Nonempty Blocks of image. */
	public ArrayList<Block> getBlocks() 
	{
		ArrayList<Block> all = new ArrayList<Block>();
		for(int i=0; i<M; i++) {
			for(int j=0; j<N; j++) {
				if(B[i][j] != null)  all.add(B[i][j]);
			}
		}
		return all;
	}

	/** 
	 * 
	 * @param sizeCutoff A fraction on [0,1] specifying the cutoff block count for the components in
	 * terms of the largest component size.
	 */
	public ArrayList<HashSet<Block>> getLargeConnectedComponents(double sizeCutoff) 
	{
		if(sizeCutoff<0 || sizeCutoff>1) 
			throw new IllegalArgumentException("sizeCutoff must be on [0,1] but was "+sizeCutoff);

		// FIND SIZE OF LARGEST CC:
		int maxSize = 0;
		for(HashSet<Block> cc : components) 
			if(cc.size() > maxSize)  maxSize = cc.size();

		System.out.println("ImageBlocker.getConnectedComponentsAboveThreshold: maxSize="+maxSize);

		// COLLECT CC ABOVE THRESHOLD:
		ArrayList<HashSet<Block>> result = new ArrayList<HashSet<Block>>();
		int cutoffSize = (int)(maxSize * sizeCutoff);
		for(HashSet<Block> cc : components) {
			if(cc.size() > cutoffSize)  result.add(cc);
		}
		return result;
	}


	/** Constructs new RigidBody objects for each connected component
	 * of Blocks, and adds each to the RigidBodySystem. */
	/*KM
      public void addComponentRigidBodies(RigidBodySystem R) 
      {
      for(HashSet<Block> cc : components) 
      R.add( new RigidBody(cc) );
      }
	 */

	private void findConnectedComponents()
	{
		ScanlineCCBuilder ccb = new ScanlineCCBuilder();
	}


	/** 
	 * Finds connected components by unifying row/column fragment
	 * labels via iterative minimization of row/col fragment
	 * labels using expansion moves.
	 */
	class ScanlineCCBuilder
	{
		private ArrayList<RowFrag>[] rowFrag; // of Frag
		private ArrayList<ColFrag>[] colFrag; // of Frag
		private int[][]     label;

		ScanlineCCBuilder() 
		{
			/// FIND CONNECTED COMPONENTS:
			System.out.println("\nFINDING CONNECTED COMPONENTS...");

			buildRowFragments();// --> rowFrag[i]
			buildColFragments();// --> colFrag[j]

			minimizeConnectedLabels();

			extractComponents(); // --> components

			System.out.println("FOUND "+components.size()+" RIGID BODIES");
		}

		/** Iterative minimization of connected labels using
		 * alternating row/column fragment minimization. */
		void minimizeConnectedLabels() 
		{
			/// INITIALIZE LABELS: 
			label = new int[M][N];
			for(int i=0; i<M; i++) 
				for(int j=0; j<N; j++) 
					label[i][j] = Integer.MAX_VALUE;

			/// INIT TO ROW FRAG/COMPONENT LABELS:
			int fragCount = 0;
			for(int i=0; i<M; i++) {
				ArrayList<RowFrag> frags = (ArrayList<RowFrag>)rowFrag[i];
				for(RowFrag frag : frags) {
					for(Block block : frag.getBlocks()) {
						label[block.i()][block.j()] = fragCount;
					}
					fragCount++;
				}
			}

			/// ITERATE ROW/COLUMN LABEL MINIMIZATION (a simple
			/// "expansion move" ... a fun thing to do at Cornell):
			int nChangesThisIteration = 1;
			while(nChangesThisIteration > 0) {/// STOP IF EXPANSION
				/// MOVE HAS NO CHANGES
				nChangesThisIteration = 0;

				/// ROW EXPANSION:
				for(ArrayList<RowFrag> frags : rowFrag) {
					for(RowFrag frag : frags) {
						int     minLabel = frag.getMinLabel();
						boolean changed  = frag.setMinLabel(minLabel);
						if(changed) nChangesThisIteration++;
					}
				}

				/// COL EXPANSION:
				for(ArrayList<ColFrag> frags : colFrag) {
					for(ColFrag frag : frags) {
						int     minLabel = frag.getMinLabel();
						boolean changed  = frag.setMinLabel(minLabel);
						if(changed) nChangesThisIteration++;
					}
				}

				System.out.println("ITERATE: #label changes = "+nChangesThisIteration);
			}
		}

		/** Extracts "components" from label */
		void extractComponents()
		{
			// MAP label-->blocks
			HashMap<Integer,HashSet<Block>> map = new HashMap<Integer,HashSet<Block>>();

			for(int i=0; i<M; i++) {
				for(int j=0; j<N; j++) {
					int lab = label[i][j];
					if(lab < Integer.MAX_VALUE) {//valid label:
						HashSet<Block> comp = map.get(lab);
						if(comp==null) {
							comp = new HashSet<Block>();
							map.put(lab, comp);
						}
						comp.add(B[i][j]);
					}
				}
			}

			// EXTRACT COMPONENTS:
			HashSet<HashSet<Block>> values = new HashSet<HashSet<Block>>();
			values.addAll(map.values());
			for(HashSet<Block> set : values) {
				components.add(set);
			}
		}

		void buildColFragments()
		{
			colFrag = new ArrayList[N];

			System.out.print("BUILDING COL FRAGMENTS ");
			int count = 0;
			for(int j=0; j<N; j++) {
				colFrag[j] = new ArrayList<ColFrag>();

				ColFrag fragment = null;
				for(int i=0; i<M; i++) {
					if(B[i][j] == null) {
						fragment = null;
					}
					else {
						if(fragment==null)   {
							fragment = new ColFrag(j);
							colFrag[j].add(fragment);
							count++;
						}

						fragment.add(B[i][j]);
					}
				}
				if(j%(N/10)==0) System.out.print(".");
			}
			System.out.println(" DONE ("+count+" col fragments)");
		}

		void buildRowFragments()
		{
			rowFrag = new ArrayList[M];

			System.out.print("BUILDING ROW FRAGMENTS ");
			int count = 0;
			for(int i=0; i<M; i++) {
				rowFrag[i] = new ArrayList<RowFrag>();

				RowFrag fragment = null;
				for(int j=0; j<N; j++) {
					if(B[i][j] == null) {
						fragment = null;
					}
					else {
						if(fragment==null)   {
							fragment = new RowFrag(i);
							rowFrag[i].add(fragment);
							count++;
						}

						fragment.add(B[i][j]);
					}
				}
				if(rowFrag[i].size() > 0) System.out.print(" "+rowFrag[i].size()+" ");
				if(i%(M/10)==0) System.out.print(".");
			}
			System.out.println(" DONE ("+count+" row fragments)");
		}

		class RowFrag
		{
			ArrayList<Block> blocks = new ArrayList<Block>();
			int index;

			///index=i
			RowFrag(int index) { this.index = index; }

			public void add(Block block) { blocks.add(block); }

			public ArrayList<Block> getBlocks() { return blocks; }

			int  getMinLabel()
			{
				int min = Integer.MAX_VALUE;
				for(Block block : blocks) { 
					int j = block.j();
					min = Math.min(min, label[index][j]);
					if(index > 0) {//Check previous row too (faster propagation)
						min = Math.min(min, label[index-1][j]);
						/// NOTE: Effectively ignores B=null entries, since
						/// they have label=Integer.MAX_VALUE

						if(j>0)   min = Math.min(min, label[index-1][j-1]);
						if(j+1<N) min = Math.min(min, label[index-1][j+1]);
					}

					// 		    if(index+1 < M) {//Check previous row too (faster propagation)
					// 			min = Math.min(min, label[index+1][j]);
					// 			/// NOTE: Effectively ignores B=null entries, since
					// 			/// they have label=Integer.MAX_VALUE

					// 			if(j>0)   min = Math.min(min, label[index+1][j-1]);
					// 			if(j+1<N) min = Math.min(min, label[index+1][j+1]);
					// 		    }
				}
				return min;
			}

			/**
			 * @return True if some values changed.
			 */
			boolean setMinLabel(int min) {
				boolean changed = false;
				for(Block block : blocks)  {
					if(label[index][block.j()] != min)  changed = true;
					label[index][block.j()] = min;
				}
				return changed;
			}
		}

		/** Col impl of Frag */
		class ColFrag
		{
			ArrayList<Block> blocks = new ArrayList<Block>();
			int index;//col

			ColFrag(int index) { this.index = index; }

			public void add(Block block) { blocks.add(block); }

			public ArrayList<Block> getBlocks() { return blocks; }

			int  getMinLabel()
			{
				int min = Integer.MAX_VALUE;
				for(Block block : blocks) { 
					int i = block.i();
					min = Math.min(min, label[i][index]);
					if(index > 0) {//Check previous col too (faster propagation)
						min = Math.min(min, label[i][index-1]);
						/// NOTE: Effectively ignores B=null entries, since
						/// they have label=Integer.MAX_VALUE

						if(i > 0)  min = Math.min(min, label[i-1][index-1]);
						if(i+1<M)  min = Math.min(min, label[i+1][index-1]);
					}

					if(index+1 < N) {
						min = Math.min(min, label[i][index+1]);
						if(i > 0)  min = Math.min(min, label[i-1][index+1]);
						if(i+1<M)  min = Math.min(min, label[i+1][index+1]);
					}



				}
				return min;
			}

			boolean setMinLabel(int min) {
				boolean changed = false;
				for(Block block : blocks)  {
					if(label[block.i()][index] != min) changed = true;
					label[block.i()][index] = min;
				}
				return changed;
			}
		}//ColFrag
	}

	/** Builds nonwhite blocks. */
	private void buildBlocks()
	{
		//int nx = ; 
		M = image.getHeight();
		N = image.getWidth();
		B = new Block[M][N];

		int maxMN = (int)Math.max(M,N);
		double  h = 0.5/(double)maxMN;//halfwidth 

		Raster raster = image.getData();

		//bytes.rewind();
		int nnz = 0;
		for(int i=0; i<M; i++) {
			for(int j=0; j<N; j++) {
				// KM: Scale larger dimension to be [0,1]
				//     (x,y) are the centers of the pixels
				double  x = h*(2*j + 1);
				double  y = h*(2*i + 1);
				Point2d p = new Point2d(x,y);

				// KM: gettting pixel
				int[] colorsArray = new int[3];
				colorsArray = raster.getPixel(j,i,colorsArray);
				int r = colorsArray[0];
				int g = colorsArray[1];
				int b = colorsArray[2];
				Color c = new Color(r,g,b);

				/// RECORD MASSIVE BLOCKS:
				Block block = new Block(i, j, c, p, h);
				if(block.getColorMass() > 0.01) {
					B[i][j] = block;
					nnz++;
				}
			}
		}
		System.out.println("Built "+nnz+" blocks / "+(M*N)+" or "+((float)nnz/(float)(M*N)*100.f)+"%");
	}

	private static BufferedImage loadImage(String fileName) throws IOException
	{
		BufferedImage img = ImageIO.read(new File(fileName));
		System.out.println("image('"+fileName+"'): height="+img.getHeight()+" width="+img.getWidth());

		return img;
	}


	/** Parses next BGR triple. */
	/*
       private static Color3f nextColor(Raster raster) 
       {
       float b = nextChannel(bytes);
       float g = nextChannel(bytes);
       float r = nextChannel(bytes);
       return new Color3f(r,g,b);
       }
	 */

	/**KM
       private static float nextChannel(ByteBuffer bytes) 
       {
       int r = (int)bytes.get();
       r = (r + 256) % 256;
       float x = (float)r/255.f;
       if(x > 1.f) x = 1.f;
       if(x < 0.f) x = 0.f;
       //System.out.println("r = "+r+" --> x="+x);
       return x;
       }
	 */

	//KM
	public static void main(String[] args) {
		try {
			ImageBlocker blocker = new ImageBlocker("lcp.bmp");
			int nRows = blocker.M;
			int nCols = blocker.N;
			int nComps = blocker.components.size();
			System.out.println("# rows = " + nRows);
			System.out.println("# cols = " + nCols);
			System.out.println("# components = " + nComps);

			System.out.println("Blocks: ");
			for (Block[] bArray: blocker.B) {
				for (Block b: bArray)
					System.out.println(b);
			}

			for (HashSet<Block> comp: blocker.components) {
				System.out.println("Component:");
				Iterator<Block> iter = comp.iterator();
				while (iter.hasNext()) {
					Block b = iter.next();
					System.out.println(b);
				}
			}
		}
		catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
