package packagedata;

import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import static packagefx.InterfaceFX.CANVAS_HEIGHT;
import static packagefx.InterfaceFX.CANVAS_WIDTH;
import static packagefx.InterfaceFX.HGAP_DEFAULT;
import static packagefx.InterfaceFX.VGAP_DEFAULT;

/**
 * Randomly generates a circuit with resistors in parallel through which the user
 * can interactively set and observe the relationship between certain variables.
 * @author patrickspensieri
 */
public class ResistorsInParallelData extends AbstractData
{
    // USER INTERFACE FIELDS //////////////////////////////////////////////////////////
    private GridPane toggleControlPane;
    private final String helpUIStringResistor = "Input desired resistance here";
    
    private static final double BATTERY_GAP = 2.0;
    private static final double DEF_STROKE_W = 1.0; 
    
    private Label[] rLabelArray;
    private Slider[] rSliderArray;
    private Pane animPane;
    private Circle[] circleArray;             //array holding electrons (circle objects)
   
    // END USER INTERFACE FIELDS //////////////////////////////////////////////////////
    
    // DATA FIELDS ////////////////////////////////////////////////////////////////////
    final private int MIN_NUMBER_RES = 2;
    final private int MAX_NUMBER_RES = 5;       
    final private double MIN_VALUE_RES = 1.0; // in ohms
    final private double MAX_VALUE_RES = 10.0; // in ohms
    final private double DEFAULT_VALUE_RES = 5.0; // in ohms
    
    private static int numberRes;           //RANDOMLY GENERATED NUMBER OF RESISTORS
    private DoubleProperty voltage;
    private DoubleProperty[] rArray;
    private DoubleBinding req;
    
    private DoubleBinding current; // = req.divide(voltage); // in amperes
    private double electronX;
    private double electronY;
    private LineTo[][] lineToArray;
    // END DATA FIELDS ////////////////////////////////////////////////////////////////
    
    // CONSTRUCTOR(S) /////////////////////////////////////////////////////////////////
    public ResistorsInParallelData()
    {   
        // initialize voltage, numberRes, and rArray
        voltage = new SimpleDoubleProperty(3.6);
        numberRes = random.nextInt(MAX_NUMBER_RES - MIN_NUMBER_RES + 1) + MIN_NUMBER_RES;
        rArray = new SimpleDoubleProperty[numberRes];
        //coordinateArray[0] is starting point for electrons (common to all)
        //coordinatePoint[1] is ending point for electrons (common to all)
        //coordinateArray contains (x,y) coordinates for electron paths
        //every electron travels along its respective resistor branch
        //every electron has left edge and right edge of resistor branch
        
        // initialize each rArray value and the corresponding Slider, bind the two together and
        // add listener for testing purposes
        for(int i = 0; i < numberRes; i++)
        {
            rArray[i] = new SimpleDoubleProperty(DEFAULT_VALUE_RES);
        }
        
        // initialize the reqTest DoubleBinding
        req = new DoubleBinding() 
        {
            {
                for(int i = 0; i < rArray.length; i++)
                    super.bind(rArray);
            }
 
            @Override
            protected double computeValue() 
            {
                double x = 0;
                for(int i = 0; i < numberRes; i++)
                {
                    x += (1 / rArray[i].getValue());
                }
                return (Math.pow(x, -1));
            }
        };
        
        // initialize the current DoubleBinding
        current = new DoubleBinding()
        {
            {
                super.bind(req);
            }
            
            @Override
            protected double computeValue()
            {
                return (voltage.get() / req.get());
//                return String.format(Locale.ENGLISH, "%.2f", someDouble);
            }
        };
    }
    
    // END CONSTRUCTOR(S) /////////////////////////////////////////////////////////////
    
    // USER INTERFACE METHODS ////////////////////////////////////////////////////////
    
    /**
     * Builds and returns GridPane object containing all toggle controls, with their respective listeners.
     * @return : toggleControlPane containing rows of label and sliders for each resistor 
     */
    @Override
    protected GridPane buildToggleControl()
    {
        //disable the pause and continue button
        actionButtonArray[1].setDisable(true);
        actionButtonArray[2].setDisable(true);
        
        toggleControlPane = new GridPane();
        toggleControlPane.setHgap(HGAP_DEFAULT);
        toggleControlPane.setVgap(VGAP_DEFAULT);
                
        rLabelArray = new Label[numberRes];
        rSliderArray = new Slider[numberRes];
        
        for(int i = 0; i < numberRes; i++)
        {
            rLabelArray[i] = new Label("Resistor " + i);
            rSliderArray[i] = new Slider(MIN_VALUE_RES, MAX_VALUE_RES, DEFAULT_VALUE_RES);
            Bindings.bindBidirectional(rArray[i], rSliderArray[i].valueProperty());
            rLabelArray[i].textProperty().bind(Bindings.format("Res " + (i + 1) + " : %.2f Ω", rArray[i]));
            toggleControlPane.addRow(i, rLabelArray[i], rSliderArray[i]);
        }
        return toggleControlPane;
    }  
    
    /**
     * Builds and return primary chart.
     * @return 
     */
    @Override
    protected Chart buildChart1()
    {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Equivalent Resistance and Resistors");
        xAxis.setAnimated(false);
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Resistance (ohms)");
        yAxis.setAutoRanging(false);
        yAxis.setAnimated(false);
        yAxis.setMinorTickCount(0);
        yAxis.setUpperBound(MAX_VALUE_RES);
        yAxis.setTickUnit(1);
        final BarChart<String,Number> barChart = new BarChart<String,Number>(xAxis,yAxis);        
        final XYChart.Series<String,Number> series = new XYChart.Series();
        barChart.setLegendVisible(false);
        
        Data reqData = new XYChart.Data("Req", req);
        reqData.YValueProperty().bind(req);
        series.getData().add(reqData);
        
        Data[] resDataArray = new Data[numberRes];
        for(int j = 0; j < numberRes; j++)
        {
            resDataArray[j] = new XYChart.Data("Res " + j, rArray[j]);
            resDataArray[j].YValueProperty().bind(rArray[j]);
            series.getData().add(resDataArray[j]);
        }
        barChart.getData().add(series);  
        return barChart; 
    }
    
    /**
     * Builds the secondary chart.
     * @return 
     */
    @Override
    protected Node buildChart2()
    {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Equivalent Resistance");
        xAxis.setUpperBound((int)Math.round(getMaxReq()) + 1);
        xAxis.setAutoRanging(false);
        xAxis.setAnimated(false);
        xAxis.setMinorTickCount(0);
        xAxis.setTickUnit(.5);
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Current (amps)");
        yAxis.setUpperBound((int)Math.round(getMaxCurrent()) + 1);
        yAxis.setAutoRanging(false);
        yAxis.setAnimated(false);
        yAxis.setMinorTickCount(0);
        yAxis.setTickUnit(.5);
        final ScatterChart<Number,Number> scatterChart = new ScatterChart<Number,Number>(xAxis,yAxis);                
        final XYChart.Series<Number,Number> series = new XYChart.Series();
        scatterChart.setLegendVisible(false);
        
        Data dataPoint = new XYChart.Data(req, current);
        dataPoint.XValueProperty().bind(req);
        dataPoint.YValueProperty().bind(current);
        series.getData().add(dataPoint);
        scatterChart.getData().add(series);
            
        return scatterChart; 
    }
    
    /**
     * Builds and returns primary Animation.
     * @return 
     */
    @Override
    protected Node buildAnimation()
    {
        animPane = new Pane();
        animPane.setPrefSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        Line[] lineArray = new Line[5*numberRes + 6]; //maximum number of lines with respect to numberRes
        //right branch
        lineArray[0] = new Line(0.9*CANVAS_WIDTH, 0.1*CANVAS_HEIGHT, 0.9*CANVAS_WIDTH, 0.9*CANVAS_HEIGHT);
        //left branch
        lineArray[1] = new Line(0.1*CANVAS_WIDTH, 0.9*CANVAS_HEIGHT, 0.1*CANVAS_WIDTH, 0.1*CANVAS_HEIGHT);
        //right segment of bottom branch
        lineArray[2] = new Line(0.9*CANVAS_WIDTH, 0.9*CANVAS_HEIGHT, (0.5*CANVAS_WIDTH + BATTERY_GAP/2 + DEF_STROKE_W),
                0.9*CANVAS_HEIGHT);
        //left segment of bottom branch
        lineArray[3] = new Line((0.5*CANVAS_WIDTH - BATTERY_GAP/2 - DEF_STROKE_W), 0.9*CANVAS_HEIGHT, 0.1*CANVAS_WIDTH,
                0.9*CANVAS_HEIGHT);
        //draw battery symbol (short line)
        lineArray[4] = new Line((0.5*CANVAS_WIDTH + BATTERY_GAP/2 + DEF_STROKE_W), (0.9*CANVAS_HEIGHT - 5), 
                (0.5*CANVAS_WIDTH + BATTERY_GAP/2 + DEF_STROKE_W), (0.9*CANVAS_HEIGHT + 5));
        //draw battery symbol (long line)
        lineArray[5] = new Line((0.5*CANVAS_WIDTH - BATTERY_GAP/2 - DEF_STROKE_W), (0.9*CANVAS_HEIGHT - 9),
                (0.5*CANVAS_WIDTH - BATTERY_GAP/2 - DEF_STROKE_W), (0.9*CANVAS_HEIGHT + 9));
 
        //assign electron starting coordinates(electronX, electronY)
        electronX = (0.5*CANVAS_WIDTH - BATTERY_GAP/2 - DEF_STROKE_W);
        electronY = (0.9*CANVAS_HEIGHT);
        
        // one row of MoveTo objects for each electron/resistor
        // lineToArray[i][0] = lower left corner of circuit
        // ...
        // lineToArray[i][4] = starting point (electronX, electronY)
        lineToArray = new LineTo[numberRes][5];
        
        //CODE TO DRAW BRANCHES AND RESISTORS
        //gaps between resistor branches
        double resBranchGap = ((0.9 - 0.1)*CANVAS_HEIGHT) / (numberRes);
        //amplitude of resistor (resistor contains three line segments)
        double resAmplitude = 0.15 * resBranchGap;
        //draw resistor branches, from TOP TO BOTTOM
        for(int i = 0; i < numberRes; i++)
        {
            //lower left circuit corner
            lineToArray[i][0] = new LineTo((0.1*CANVAS_WIDTH), (0.9*CANVAS_HEIGHT));
            //upper left circuit corner
            lineToArray[i][1] = new LineTo((0.1*CANVAS_WIDTH), (0.1*CANVAS_HEIGHT + resBranchGap*i));
            //upper right circuit corner
            lineToArray[i][2] = new LineTo((0.9*CANVAS_WIDTH), (0.1*CANVAS_HEIGHT + resBranchGap*i));
            //lower right circuit corner
            lineToArray[i][3] = new LineTo((0.9*CANVAS_WIDTH), (0.9*CANVAS_HEIGHT));
            //starting point of electron(electronX, electronY)
            lineToArray[i][4] = new LineTo(electronX, electronY);
            
            //left segment
            lineArray[5*(i+1)+1] = new Line((0.1*CANVAS_WIDTH), (0.1*CANVAS_HEIGHT + resBranchGap*i), (0.45*CANVAS_WIDTH),
                    (0.1*CANVAS_HEIGHT + resBranchGap*i));
            //right segment
            lineArray[5*(i+1)+2] = new Line((0.55*CANVAS_WIDTH), (0.1*CANVAS_HEIGHT + resBranchGap*i), (0.9*CANVAS_WIDTH),
                    (0.1*CANVAS_HEIGHT + resBranchGap*i));
            //resistor
            lineArray[5*(i+1)+3] = new Line((0.45*CANVAS_WIDTH), (0.1*CANVAS_HEIGHT + resBranchGap*i), (0.45*CANVAS_WIDTH +(0.55-0.45)*CANVAS_WIDTH/4),
                    (0.1*CANVAS_HEIGHT + resBranchGap*i - resAmplitude));
            lineArray[5*(i+1)+4] = new Line((0.45*CANVAS_WIDTH +(0.55-0.45)*CANVAS_WIDTH/4), (0.1*CANVAS_HEIGHT + resBranchGap*i - resAmplitude),
                    (0.45*CANVAS_WIDTH + (3*(0.55-0.45)*CANVAS_WIDTH/4)), (0.1*CANVAS_HEIGHT + resBranchGap*i + resAmplitude));
            lineArray[5*(i+1)+5] = new Line((0.45*CANVAS_WIDTH + (3*(0.55-0.45)*CANVAS_WIDTH/4)), (0.1*CANVAS_HEIGHT + resBranchGap*i + resAmplitude),
                    (0.55*CANVAS_WIDTH), (0.1*CANVAS_HEIGHT + resBranchGap*i));
        }
        // add all lines
        for(int j = 0; j < lineArray.length; j++)
        {
            animPane.getChildren().add(lineArray[j]);
        }

        return animPane; 
    }
    
    /**
     * Resets all resistor values to default.
     */
    @Override
    protected void reset()
    {
        for(int i = 0; i < rArray.length; i++)
            rArray[i].setValue(DEFAULT_VALUE_RES);
        
        for(int j = 0; j < circleArray.length; j++)
            animPane.getChildren().remove(circleArray[j]);
        
        //enable the start button to be clicked
        actionButtonArray[0].setDisable(false);
        //disable the pause and continue buttons
        actionButtonArray[1].setDisable(true);
        actionButtonArray[2].setDisable(true);
    }
    
    /**
     * Starts the ResistorsInParallelAnimation animation.
     */
    @Override
    protected void start()
    {   
        //disable the start button
        actionButtonArray[0].setDisable(true);
        //enable the pause button
        actionButtonArray[1].setDisable(false);
        
        ParallelTransition parTransition = new ParallelTransition();
        parTransition.setAutoReverse(true);
        // VERY IMPORTANT LINE
        animation = parTransition;
        
        // array holding the electrons (circle objects)
        circleArray = new Circle[numberRes];
        
        // array of path objects for each of the electrons
        Path[] pathArray = new Path[numberRes];
        // array of PathTransition objects for each of the electrons
        PathTransition[] pathTransitionArray = new PathTransition[numberRes];
        for(int i = 0; i < numberRes; i++)
        {
            // create an electron for each resistor branch and add to animPane ... new Circle(xPos, yPos, rad, col);
            circleArray[i] = new Circle(electronX, electronY, 5.0, Color.GOLD);
            animPane.getChildren().add(circleArray[i]);
            // create a path for each electron
            pathArray[i] = new Path();
            pathArray[i].getElements().add(new MoveTo(electronX, electronY));
            pathArray[i].getElements().add(lineToArray[i][0]);
            pathArray[i].getElements().add(lineToArray[i][1]);
            pathArray[i].getElements().add(lineToArray[i][2]);
            pathArray[i].getElements().add(lineToArray[i][3]);
            pathArray[i].getElements().add(lineToArray[i][4]);
                        
            // create a PathTransition for each electron and add to main parTransition
            pathTransitionArray[i] = new PathTransition(Duration.seconds(5), pathArray[i], circleArray[i]);
            parTransition.getChildren().add(pathTransitionArray[i]);
        }   
        parTransition.play();
        //remove electrons when animation is finished
        parTransition.setOnFinished(new EventHandler<ActionEvent>() 
        {
            @Override
            public void handle(ActionEvent e) 
            {
                for(int j = 0; j < circleArray.length; j++)
                {
                    animPane.getChildren().remove(circleArray[j]);
                    //enable the start button when start() method is completed
                    actionButtonArray[0].setDisable(false);
                    actionButtonArray[1].setDisable(true);
                    actionButtonArray[2].setDisable(true);
                }
            }
        });    
    }
    
    
    // END USER INTERFACE METHODS ////////////////////////////////////////////////////
    
    // DATA METHODS /////////////////////////////////////////////////////////////////
    
    /**
     * Returns maximum current.
     * @return : voltage / minReq
     */
    private double getMaxCurrent()
    {
        return (voltage.get() / getMinReq());
    }
    
    /**
     * Gets maximum equivalent resistance.
     * @return : maxReq
     */
    private double getMaxReq()
    {
        double maxReq = 0;
        for(int i = 0; i < numberRes; i++)
        {
            maxReq += (1 / MAX_VALUE_RES);
        }
        return (Math.pow(maxReq, -1));
    }
    
    /**
     * Gets maximum equivalent resistance.
     * @return : maxReq
     */
    private double getMinReq()
    {
        double minReq = 0;
        for(int i = 0; i < numberRes; i++)
        {
            minReq += (1 / MIN_VALUE_RES);
        }
        return (Math.pow(minReq, -1));
    }
   
    // END DATA METHODS /////////////////////////////////////////////////////////////     
}
    