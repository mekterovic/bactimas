package bactimas.bintree;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

import javax.swing.JPanel;

public class BTreeVisualizationPane extends JPanel

{

	private static final long serialVersionUID = 1401020454475590665L;

	private Font font;
	private final static Font fontCells;
	//private static final int FONT_SIZE = 18;
	private static final int FONT_SIZE_CELLS = 9;
	private static final int COLOR_LEGEND_HEIGHT = 30;
	static BasicStroke stroke;

	static BasicStroke basicStroke;

	static final float[] dash1;

	static final BasicStroke dashed;

	private Node _root;

	private int _maxFrame;

	private double _minInt;

	private double _maxInt;

	private float verticalMargin;

	private float horMargin;

	private boolean _drawLabel;

	private boolean _convertToHours;

	int width;

	int height;

	Color[] _palette;

	int _minStrokeWidth;

	int _maxStrokeWidth;

	double _minSize;

	double _maxSize;

	private static int _tickStep;
	
	private LinkedList<BTreeEvent> _events;

	private String _onColorLabel, _onWidthLabel ;
	
	static {

		
		fontCells = new Font("Times New Roman", 0, FONT_SIZE_CELLS);

		BTreeVisualizationPane.stroke = new BasicStroke(3.0f);

		BTreeVisualizationPane.basicStroke = new BasicStroke(1.0f);

		dash1 = new float[] { 10.0f };

		dashed = new BasicStroke(3.0f, 0, 0, 10.0f,
				BTreeVisualizationPane.dash1, 0.0f);

	}

	public BTreeVisualizationPane(
			final Node root, 
			final int maxLevel,
			final int maxFrame, 
			final LinkedList<Color> palette,
			final double minInt, 
			final double maxInt, 
			final int minStrokeWidth,
			final int maxStrokeWidth, 
			final double minSize,
			final double maxSize, 
			final boolean drawLabel,
			final boolean convertToHours,
			final LinkedList<BTreeEvent> events,
			final String onColorLabel,
			final String onWidthLabel, 
			final int fontSize) {

		super();
		
		_events = events;
		
		font = new Font("Times New Roman", Font.BOLD, fontSize);
		
		this.verticalMargin = 0.05f;

		this.horMargin = 0.05f;

		this._minStrokeWidth = minStrokeWidth;

		this._maxStrokeWidth = maxStrokeWidth;

		this._minSize = minSize;

		this._maxSize = maxSize;

		BTreeVisualizationPane.stroke = new BasicStroke(
				1.0f * (float) this._minStrokeWidth, 0, 0);

		this._root = root;

		this._maxFrame = maxFrame;

		this._minInt = minInt;

		this._maxInt = maxInt;

		this._drawLabel = drawLabel;

		this._convertToHours = convertToHours;
		
		_onColorLabel = onColorLabel;
		_onWidthLabel = onWidthLabel;

		BTreeVisualizationPane._tickStep = getTickStep(this._maxFrame);

		final float segSize = 100.0f / (float) (palette.size() - 1);

		final Iterator<Color> it = palette.iterator();

		Color from = (Color) it.next();

		int segi = 0;

		this._palette = new Color[100];

		while (it.hasNext()) {

			final Color to = (Color) it.next();

			//System.out.println("from=" + from + "   to=" + to);

			for (int i = 0; (float) i < segSize; ++i, ++segi) {

				this._palette[segi] = new Color(this.max255((float) from
						.getRed()
						+ (float) (i * (to.getRed() - from.getRed()))
						/ segSize), this.max255((float) from.getGreen()
						+ (float) (i * (to.getGreen() - from.getGreen()))
						/ segSize), this.max255((float) from.getBlue()
						+ (float) (i * (to.getBlue() - from.getBlue()))
						/ segSize));

			}

			from = to;

		}

		this.setOpaque(false);

	}

	private static int getTickStep(final int max) {

		int ts = 0;

		final int[] tresholds = { 30, 15, 6, 3 };

		final int[] steps = { 5, 2, 1 };

		while (ts == 0) {

			if (max <= tresholds[0]) {

				for (int i = 0; i < steps.length; ++i) {

					if (max <= tresholds[i] && max > tresholds[i + 1]) {

						ts = steps[i];

						break;

					}

				}

				if (ts == 0) {

					ts = 1;

				}

			}

			else {

				int i;

				for (i = 0; i < steps.length; ++i) {

					final int[] array = tresholds;

					final int n = i;

					array[n] = array[n] * 10;

					final int[] array2 = steps;

					final int n2 = i;

					array2[n2] = array2[n2] * 10;

				}

				final int[] array3 = tresholds;

				final int n3 = i;

				array3[n3] = array3[n3] * 10;

			}

		}

		return ts;

	}

	private int max255(final float c) {

		if (c > 255.0f) {

			System.out.println(new StringBuilder("!!c=").append(c).toString());

			return 255;

		}

		return (int) c;

	}

	private int getNodeX(final Node n) {

		final double strip = (double) ((float) this.width - 2.0f
				* this.horMargin * (float) this.width)
				/ Math.pow(2.0, (double) n.getLevel());

		final int x = (int) (strip * (double) n.getLevelIndex() + strip / 2.0 + (double) (this.horMargin * (float) this.width));

		return x;

	}

	private int getNodeY(final Node n) {

		return this.getYforFrame(n.getFrame());

	}

	private int getYforFrame(final int f) {

		return (int) (this.verticalMargin * (float) this.height + ((float) this.height - 2.0f
				* this.verticalMargin * (float) this.height)
				/ (float) this._maxFrame * (float) f);

	}

	private float getStrokeForSize(final double size) {
		if (_maxSize == _minSize && _maxSize == 0) return _minStrokeWidth;  // --none--
		
		return (float) ((size - this._minSize)
				/ (this._maxSize - this._minSize)
				* (double) (this._maxStrokeWidth - this._minStrokeWidth) + (double) this._minStrokeWidth);

	}

	private float getSizeForStroke(final double sw) {		
		
		return (float) ( (sw - _minStrokeWidth)	/ (_maxStrokeWidth - _minStrokeWidth) * (_maxSize - _minSize)  + _minSize);

	}	
	private void drawNode(final Graphics g, final Node n) {

		final int x = this.getNodeX(n);

		final int y = this.getNodeY(n);

		g.setColor(Color.BLACK);

		if (this._drawLabel) {
			g.setFont(fontCells);
			g.drawString(n.getName(),
					x - g.getFontMetrics().stringWidth(n.getName()) / 2, y - 6);
			g.setFont(font);
		}

		((Graphics2D) g).setStroke(BTreeVisualizationPane.stroke);

		final ArrayList<Node.TimeIntensity> timeInts = n.getIntensityList();

		final ArrayList<Node.TimeSize> timeSizes = n.getSizeList();

		int currY = y + 1;

		int i = 0;

		int j = 0;

		while (i < timeInts.size() || j < timeSizes.size()) {

			if (i < timeInts.size()
					&& (j == timeSizes.size() || ((Node.TimeIntensity) timeInts
							.get(i))._time <= ((Node.TimeSize) timeSizes.get(j))._time)) {

				int idx;
				
				if (_minInt == _maxInt && _minInt == 0) {  // --none --
					idx = 0;
				} else {
					idx = (int) ((((Node.TimeIntensity) timeInts.get(i))._intensity - this._minInt)
							/ (this._maxInt - this._minInt + 1.0) * 100.0);
				}
				
				if (idx >= 0 && idx < 100) {

					g.setColor(this._palette[idx]);

				}

				else {

					g.setColor(Color.BLACK);

				}

				if (i < timeInts.size() - 1) {

					g.drawLine(x, currY, x, this
							.getYforFrame(((Node.TimeIntensity) timeInts
									.get(i + 1))._time));

					currY = this.getYforFrame(((Node.TimeIntensity) timeInts
							.get(i + 1))._time);

				}

				++i;

			}

			else {

				g.drawLine(x, currY, x, this
						.getYforFrame(((Node.TimeSize) timeSizes.get(j))._time));

				currY = this
						.getYforFrame(((Node.TimeSize) timeSizes.get(j))._time);

				((Graphics2D) g)
						.setStroke(new BasicStroke(this
								.getStrokeForSize(((Node.TimeSize) timeSizes
										.get(j))._size), 0, 0));

				++j;

			}
			

			
			

		}
		// mark state changes:
		LinkedList <BTreeStateChange> stateChanges = n.getStateChanges();
		if (stateChanges != null){
			g.setColor(Color.BLACK);
			for (BTreeStateChange bsc : stateChanges) {
				g.drawString(bsc.getStateTag(),
						x + _maxStrokeWidth/2 + 3,
						getYforFrame(bsc.getFrameNo()));
			}
		}
		// end mark...

		if (n.getLeftChild() != null && n.getRightChild() != null) {

			g.drawLine(x, currY, x, this.getNodeY(n.getLeftChild()));

			((Graphics2D) g).setStroke(BTreeVisualizationPane.basicStroke);

			g.setColor(Color.BLACK);

			g.drawLine(x, this.getNodeY(n.getLeftChild()),
					this.getNodeX(n.getLeftChild()),
					this.getNodeY(n.getLeftChild()));

			g.drawLine(x, this.getNodeY(n.getRightChild()),
					this.getNodeX(n.getRightChild()),
					this.getNodeY(n.getRightChild()));

			this.drawNode(g, n.getLeftChild());

			this.drawNode(g, n.getRightChild());

		}

	}

	public void paintComponent(final Graphics g1) {

		this.setBackground(Color.WHITE);

		final Graphics2D g2 = (Graphics2D) g1;

		g2.setColor(Color.BLACK);

		g2.setFont(font);

		this.width = this.getWidth();

		this.height = this.getHeight();

		this.drawNode(g1, this._root);

		g2.setStroke(BTreeVisualizationPane.basicStroke);
		// draw color legend
		if (!(_minInt == _maxInt && _minInt == 0)) { 
			for (int i = 0; i < 100; ++i) {
	
				g2.setColor(this._palette[i]);
	
				int j = 0;
	
				while (j < 3) {
	
					g2.drawLine(
							Math.round(width / 2 + (width/2-horMargin*width)/2 - 150 + i * 3 + j), 
							Math.round(verticalMargin * height + 10),
							Math.round(width / 2 + (width/2-horMargin*width)/2 - 150 + i * 3 + j), 
							Math.round(verticalMargin * height + 10 + COLOR_LEGEND_HEIGHT)
							);
	
					++j;
	
				}
	
				if (i % 20 == 0) {
	
					g2.setColor(Color.BLACK);
					
					g2.drawString(							
									"" + ((int) (this._minInt + (double) i * (this._maxInt - this._minInt) / 100.0 + 0.5)), 
									Math.round(width / 2 + (width/2-horMargin*width)/2 - 150 + i * 3),
									Math.round(verticalMargin * height + 5)
							);
	
				}
				g2.setColor(Color.BLACK);
				g2.drawString(
						new StringBuilder().append((int) (0.5 + this._maxInt)).toString(), 
						Math.round(width / 2 + (width/2-horMargin*width)/2 - 150 + 100 * 3),
						Math.round(verticalMargin * height + 5)
						);
	
			}
			g2.setColor(Color.BLACK);
			g2.drawString(_onColorLabel, 
					Math.round(width / 2 + (width/2-horMargin*width)/2 - g2.getFontMetrics().stringWidth(_onColorLabel)/2), 
					Math.round(verticalMargin * height + 10 + COLOR_LEGEND_HEIGHT + font.getSize() + 2)
					);
			
			
		}
		
		
		// draw stroke legend
		g2.setColor(Color.BLACK);
		if (!(_minSize == _maxSize && _minSize == 0)) { 
			int STROKE_LEGEND_STEPS = 5;
			for (int s = 0; s < STROKE_LEGEND_STEPS; ++s) {
	
				int sw = (int) Math.round( _minStrokeWidth + (1.0 * s)/(STROKE_LEGEND_STEPS - 1) * (_maxStrokeWidth - _minStrokeWidth));
	
				//int x = (int) (this.horMargin * (float) this.width / 2.0f);
				int x = Math.round((2 * horMargin * width + width)/4);
				
				
				for (int fs = 0; fs < font.getSize(); ++fs) {
	
					g2.drawLine(
								x - sw, 
							 	Math.round(verticalMargin * height + 10 + font.getSize() * (s-1) + 2 + fs),
								x, 
								Math.round(verticalMargin * height + 10 + font.getSize() * (s-1) + 2 + fs)
//								height - FONT_SIZE * (STROKE_LEGEND_STEPS - s) - 2 + fs								
								);				
	
				}
	
				g2.drawString(	"" + formatNumber(getSizeForStroke(sw), 2), 
						x + 3,
						Math.round(verticalMargin * height + 10 + font.getSize() *  s + 1 + 2 )
						//height - FONT_SIZE * (STROKE_LEGEND_STEPS - s - 1) - 2
						);
	
			}	
			g2.drawString(_onWidthLabel, 
					Math.round(horMargin*width + (width/2-horMargin*width)/2 - g2.getFontMetrics().stringWidth(_onWidthLabel)/2), 
					Math.round(verticalMargin * height + 10 + font.getSize() * STROKE_LEGEND_STEPS)
					);
		}
		// draw events markers
		if (_events != null)
			g2.setColor(Color.BLACK);
			g2.setStroke(BTreeVisualizationPane.dashed);
			for (BTreeEvent e : _events) {
				g2.drawLine(
						(int) (this.horMargin * (float) width), 
						getYforFrame(e.getFrameNo()),
						(int) ((1. - horMargin) * width), 
						getYforFrame(e.getFrameNo())
						);
				g2.drawString(
						e.getEventAbbr() + " (" + getTimeTagForFrame(e.getFrameNo()) + ")",
						(int) (this.horMargin * (float) width),
						getYforFrame(e.getFrameNo()) -2 						
						);
			}
		
		
		// 	draw time axis
		g2.setColor(Color.BLACK);



		g2.setStroke(BTreeVisualizationPane.dashed);

		g2.drawLine(
				(int) (this.horMargin * (float) this.width / 2.0f),
				(int) (this.verticalMargin * (float) this.height),
				(int) (this.horMargin * (float) this.width / 2.0f),
				(int) ((1.0 - (double) this.verticalMargin) * (double) this.height));

		int currFrame = 0;

		g2.drawString("Time " + ((_convertToHours == true) ? "(h)" : "(s)")
				, (int) (this.horMargin * (float) this.width/ 2.0f + 5.0f)
				, this.getYforFrame(0)/2);
		
		for (; currFrame < this._maxFrame; currFrame = currFrame + BTreeVisualizationPane._tickStep) {



			g2.drawString(getTimeTagForFrame(currFrame), 
					(int) (this.horMargin * (float) this.width/ 2.0f + 5.0f), 
					this.getYforFrame(currFrame) - 5);


			g2.drawLine(
					(int) (this.horMargin * (float) this.width / 2.0f + 5.0f),
					this.getYforFrame(currFrame),
					(int) (this.horMargin * (float) this.width),
					this.getYforFrame(currFrame));

		}

	}
	public static String formatNumber(double num, int decPlaces) {
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
		otherSymbols.setDecimalSeparator('.');
		otherSymbols.setGroupingSeparator(','); 
		String format = "0.";
		while (decPlaces-- > 0) format += "0";
		DecimalFormat df = new DecimalFormat(format, otherSymbols);
		return df.format(num);		
		
	}	
	
	private String getTimeTagForFrame (int frame) {
		if (_convertToHours) {
			int mins = frame % 3600 / 60;
			return (frame / 3600 + ":" + ((mins < 10) ? "0" : "") + mins); 
		} else {
			return frame + "";			
		}				
	}

}