package us.k5n.journal;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import us.k5n.ical.Constants;

/**
 * Main class for k5njournal application.
 * 
 * @author Craig Knudsen, craig@k5n.us
 * @version $Id$
 * 
 */
public class Main extends JFrame implements Constants {
	public static final String DEFAULT_DIR_NAME = "k5njournal";
	Frame parent;
	JLabel messageArea;
	Repository repo;
	JTree dateTree;
	DefaultMutableTreeNode dateTop;
	Vector entries;
	final static String[] monthNames = { "", "January", "February", "March",
	    "April", "May", "June", "July", "August", "September", "October",
	    "November", "December" };

	class DateFilterTreeNode extends DefaultMutableTreeNode {
		public int year, month, day;
		public String label;

		public DateFilterTreeNode(String l, int y, int m, int d) {
			super ( l );
			this.year = y;
			this.month = m;
			this.day = d;
			this.label = l;
		}

		public String toString () {
			return label;
		}
	}

	public Main() {
		this ( 600, 600 );
	}

	public Main(int w, int h) {
		super ( "k5njournal" );
		this.parent = this;
		// TODO: save user's preferred size on exit and set here
		setSize ( w, h );

		setDefaultCloseOperation ( EXIT_ON_CLOSE );
		Container contentPane = getContentPane ();

		// Create a menu bar
		setJMenuBar ( createMenu () );

		contentPane.setLayout ( new BorderLayout () );

		// Add message/status bar at bottom
		messageArea = new JLabel ( "Welcome to k5njournal..." );
		contentPane.add ( messageArea, BorderLayout.SOUTH );

		// TODO: add JToolbar at top for buttons (save, open, etc.)

		JPanel navArea = createFileSelection ();
		JPanel viewArea = createViewArea ();
		JSplitPane splitPane = new JSplitPane ( JSplitPane.VERTICAL_SPLIT, navArea,
		    viewArea );
		splitPane.setOneTouchExpandable ( true );
		splitPane.setResizeWeight ( 0.5 );
		contentPane.add ( splitPane, BorderLayout.CENTER );

		// Load data
		repo = new Repository ( getDataDirectory (), false );
		// Populate Date JTree
		updateDateTree ();

		// this.pack ();
		this.setVisible ( true );
	}

	public void setMessage ( String msg ) {
		this.messageArea.setText ( msg );
	}

	public JMenuBar createMenu () {
		JMenuItem item;

		JMenuBar bar = new JMenuBar ();

		JMenu fileMenu = new JMenu ( "File" );

		item = new JMenuItem ( "Close" );
		item.setAccelerator ( KeyStroke.getKeyStroke ( 'C', Toolkit
		    .getDefaultToolkit ().getMenuShortcutKeyMask () ) );
		item.addActionListener ( new ActionListener () {
			public void actionPerformed ( ActionEvent event ) {
				// TODO...
			}
		} );
		fileMenu.add ( item );

		item = new JMenuItem ( "Exit" );
		item.setAccelerator ( KeyStroke.getKeyStroke ( 'X', Toolkit
		    .getDefaultToolkit ().getMenuShortcutKeyMask () ) );
		item.addActionListener ( new ActionListener () {
			public void actionPerformed ( ActionEvent event ) {
				// TODO: check for unsaved changes
				// TODO: save current size of main window for use next time
				System.exit ( 0 );
			}
		} );

		fileMenu.add ( item );

		bar.add ( fileMenu );

		bar.add ( Box.createHorizontalGlue () );

		JMenu helpMenu = new JMenu ( "Help" );

		item = new JMenuItem ( "About..." );
		item.setAccelerator ( KeyStroke.getKeyStroke ( 'A', Toolkit
		    .getDefaultToolkit ().getMenuShortcutKeyMask () ) );
		item.addActionListener ( new ActionListener () {
			public void actionPerformed ( ActionEvent event ) {
				// TODO: add logo, etc...
				JOptionPane.showMessageDialog ( parent,
				    "k5njournal Version 0.1\n\nDeveloped by k5n.us\n\n"
				        + "Go to www.k5n.us for more info." );
			}
		} );
		helpMenu.add ( item );

		bar.add ( helpMenu );

		return bar;
	}

	/**
	 * Create the file selection area on the left side of the window. This will
	 * include a split pane where the top will allow navigation and selection of
	 * dates and the bottom will allow the selection of a date.
	 * 
	 * @return
	 */
	protected JPanel createFileSelection () {
		JPanel topPanel = new JPanel ();
		topPanel.setLayout ( new BorderLayout () );

		JTabbedPane tabbedPane = new JTabbedPane ();

		JPanel byDate = new JPanel ();
		byDate.setLayout ( new BorderLayout () );
		tabbedPane.addTab ( "Date", byDate );
		dateTop = new DefaultMutableTreeNode ( "All" );
		dateTree = new JTree ( dateTop );

		MouseListener ml = new MouseAdapter () {
			public void mousePressed ( MouseEvent e ) {
				int selRow = dateTree.getRowForLocation ( e.getX (), e.getY () );
				TreePath selPath = dateTree.getPathForLocation ( e.getX (), e.getY () );
				if ( selRow != -1 ) {
					if ( e.getClickCount () == 1 ) {
						dateSelected ( selRow, selPath );
					} else if ( e.getClickCount () == 2 ) {
						// Do something for double-click???
					}
				}
			}
		};
		dateTree.addMouseListener ( ml );

		JScrollPane scrollPane = new JScrollPane ( dateTree );
		byDate.add ( scrollPane, BorderLayout.CENTER );

		JPanel byCategory = new JPanel ();
		tabbedPane.addTab ( "Category", byCategory );

		JPanel bottomPane = new JPanel ();

		JSplitPane splitPane = new JSplitPane ( JSplitPane.HORIZONTAL_SPLIT,
		    tabbedPane, bottomPane );
		splitPane.setOneTouchExpandable ( true );
		splitPane.setResizeWeight ( 0.5 );
		// splitPane.setDividerLocation ( -1 );

		topPanel.add ( splitPane, BorderLayout.CENTER );

		return topPanel;
	}

	void dateSelected ( int row, TreePath path ) {
		int year = -1;
		int month = -1;
		if ( path.getPathCount () < 2 ) {
			// "All"
		} else {
			DateFilterTreeNode dateFilter = (DateFilterTreeNode) path
			    .getLastPathComponent ();
			System.out.println ( "Showing entries for " + dateFilter.year + "/"
			    + dateFilter.month );
			year = dateFilter.year;
			if ( dateFilter.month > 0 )
				month = dateFilter.month;
		}
		if ( year < 0 ) {
			entries = repo.getAllEntries ();
		} else if ( month < 0 ) {
			entries = repo.getEntriesByYear ( year );
		} else {
			entries = repo.getEntriesByMonth ( year, month );
		}
	}

	protected JPanel createViewArea () {
		// TODO
		return new JPanel ();
	}

	void updateDateTree () {
		// Remove all old entries
		dateTop.removeAllChildren ();
		// Get entries, starting with years
		int[] years = repo.getYears ();
		for ( int i = 0; years != null && i < years.length; i++ ) {
			DateFilterTreeNode yearNode = new DateFilterTreeNode ( "" + years[i],
			    years[i], 0, 0 );
			dateTop.add ( yearNode );
			int[] months = repo.getMonthsForYear ( years[i] );
			for ( int j = 0; months != null && j < months.length; j++ ) {
				DateFilterTreeNode monthNode = new DateFilterTreeNode (
				    monthNames[months[j]], years[i], months[j], 0 );
				yearNode.add ( monthNode );
			}
		}
	}

	/**
	 * Get the data directory that data files for this application will be stored
	 * in.
	 * 
	 * @return
	 */
	// TODO: allow user preferences to override this setting
	File getDataDirectory () {
		String s = (String) System.getProperty ( "user.home" );
		if ( s == null ) {
			System.err.println ( "Could not find user.home setting." );
			System.err.println ( "Using current directory instead." );
			s = ".";
		}
		File f = new File ( s );
		if ( f == null )
			fatalError ( "Invalid user.home value '" + s + "'" );
		if ( !f.exists () )
			fatalError ( "Home directory '" + f + "' does not exist." );
		if ( !f.isDirectory () )
			fatalError ( "Home directory '" + f + "'is not a directory" );
		// Use the home directory as the base. Data files will
		// be stored in a subdirectory.
		File dir = new File ( f, DEFAULT_DIR_NAME );
		if ( !dir.exists () ) {
			if ( !dir.mkdirs () )
				fatalError ( "Unable to create data directory: " + dir );
			showMessage ( "The following directory was created\n"
			    + "to store data files:\n\n" + dir );
		}
		if ( !dir.isDirectory () )
			fatalError ( "Not a directory: " + dir );
		return dir;
	}

	void showMessage ( String message ) {
		JOptionPane.showMessageDialog ( parent, message, "Notice",
		    JOptionPane.INFORMATION_MESSAGE );
	}

	void fatalError ( String message ) {
		System.err.println ( "Fatal error: " + message );
		JOptionPane.showMessageDialog ( parent, message, "Fatal Error",
		    JOptionPane.ERROR );
		System.exit ( 1 );
	}

	/**
	 * @param args
	 */
	public static void main ( String[] args ) {
		new Main ();
	}

}