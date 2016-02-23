package packagedata;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import static packagefx.InterfaceFX.CANVAS_HEIGHT;
import static packagefx.InterfaceFX.CANVAS_WIDTH;
import static packagefx.InterfaceFX.PERMEABILITY_OF_FREE_SPACE;

/**
 * Interactively demonstrates Lorentz Force through the simplified concept of a Rail Gun.
 * @author patrickspensieri
 */
public class LorentzForceData extends AbstractData
{
    // USER INTERFACE FIELDS ///////////////////////////////////////////////////
    private GridPane toggleControlPane;
    private final String helpUIStringCurrent = "Input desired current here";
    private final String helpUIStringMass = "Input desired mass here";
        
    private final int MIN_VALUE_CURRENT = 100000;               //in amperes
    private final int MAX_VALUE_CURRENT = 1000000;              //in amperes
    private final int DEFAULT_VALUE_CURRENT = 500000;           //in amperes
    
    private final int MIN_VALUE_MASS = 2;                       //in kg
    private final int MAX_VALUE_MASS = 10;                      //in kg
    private final int DEFAULT_VALUE_MASS = 5;                   //in kg
    
    private final int ANIMATION_DURATION = 3000;        //animation duration for this particular class, in milliseconds
    
    private Slider currentSlider;
    private Slider massSlider;
    private Label currentLabel;
    private Label massLabel;
  
    private NumberAxis xAxis, yAxis;
    private LineChart<Number,Number> lineChart;
    private XYChart.Series<Number,Number> series;
    
    private Pane animPane;
    private Pane tempPane;
    private Rectangle armature;
    private ImageView projectile;
    private Rectangle upperRail;
    // END USER INTERFACE FIELDS ///////////////////////////////////////////////
    
    // DATA FIELDS /////////////////////////////////////////////////////////////
    
    private double DISTANCE_BETWEEN_RODS = 1.0;     // in meters
    private double RADIUS_OF_ROD = 0.1;            // in meters
    private DoubleProperty current;             // in amps
    private DoubleProperty mass;          //in kg
    
    private DoubleBinding force;                // in Newtons
    private DoubleBinding totalTime;           // in seconds
    private DoubleBinding acceleration;         // in m/s^2
    private DoubleBinding finalVelocity;            // in m/s
    
    private int cycleCount;                     // counter used for timeline animation
        
    private boolean firstRun;               //boolean used to see if start() method was called
    // END DATA FIELDS /////////////////////////////////////////////////////////

    public LorentzForceData()
    {
        current = new SimpleDoubleProperty(DEFAULT_VALUE_CURRENT);
        mass = new SimpleDoubleProperty(DEFAULT_VALUE_MASS);
        firstRun = true;
        
        force = new DoubleBinding() 
        {
            {
                    super.bind(current);
            }
 
            @Override
            protected double computeValue() // F = ((u0)(I^2)ln(d/r)) / 2PI
            {  
                return ((PERMEABILITY_OF_FREE_SPACE * current.doubleValue() * current.doubleValue())
                        * (Math.log(DISTANCE_BETWEEN_RODS / RADIUS_OF_ROD)) 
                        / (2 * Math.PI));
            }
        };
        
        totalTime = new DoubleBinding()
        {
            {
                super.bind(force);
            }
            
            @Override
            public double computeValue() // totalTime = sqrt((2d) / a)
            {
                return Math.pow((2*DISTANCE_BETWEEN_RODS / acceleration.doubleValue()), 0.5);
            }
        };
        
        acceleration = force.divide(mass);
        
        finalVelocity = acceleration.multiply(totalTime);
    }
    
    /**
     * Builds and returns GridPane object containing all toggle controls, with their respective listeners.
     * @return 
     */
    protected GridPane buildToggleControl()
    {
        //disable the pause and continue button
        actionButtonArray[1].setDisable(true);
        actionButtonArray[2].setDisable(true);
        
        toggleControlPane = new GridPane();
        currentSlider = new Slider(MIN_VALUE_CURRENT, MAX_VALUE_CURRENT, DEFAULT_VALUE_CURRENT);
        currentSlider.setMajorTickUnit(10000.0);
        currentSlider.setMinorTickCount(0);
        currentSlider.setSnapToTicks(true);
        currentSlider.setShowTickMarks(false);
        massSlider = new Slider(MIN_VALUE_MASS, MAX_VALUE_MASS, DEFAULT_VALUE_MASS);
        massSlider.setMajorTickUnit(1.0);
        massSlider.setMinorTickCount(0);
        massSlider.setSnapToTicks(true);
        massSlider.setShowTickMarks(false);
        //bind sliders to corresponding data members
        Bindings.bindBidirectional(current, currentSlider.valueProperty());
        Bindings.bindBidirectional(mass, massSlider.valueProperty());
        
        currentLabel = new Label();
        massLabel = new Label();
        currentLabel.textProperty().bind(Bindings.format("Current : %.0f A", current));
        massLabel.textProperty().bind(Bindings.format("Mass : %.0f kg", mass));
        
        toggleControlPane.addRow(0, currentLabel, currentSlider);
        toggleControlPane.addRow(1, massLabel, massSlider);
        
        return toggleControlPane;
    }
    
    /**
     * Builds and return primary chart.
     * @return 
     */
    protected Chart buildChart1()
    {
        // setup chart
        xAxis = new NumberAxis();
        xAxis.setLabel("time (s)");
        xAxis.setUpperBound(5);
        xAxis.setAutoRanging(false);
        xAxis.setAnimated(true);
        xAxis.setMinorTickCount(0);
        xAxis.setTickUnit(1);
        
        yAxis = new NumberAxis();
        yAxis.setLabel("velocity (m/s)");
        yAxis.setUpperBound(5);
        yAxis.setAutoRanging(false);
        yAxis.setAnimated(true);
        yAxis.setMinorTickCount(0);
        yAxis.setTickUnit(1);
        
        lineChart = new LineChart<Number,Number>(xAxis,yAxis);                
        series = new XYChart.Series();
        lineChart.setLegendVisible(false);
        lineChart.getData().add(series);
        lineChart.setCreateSymbols(false);
            
        return lineChart; 
    }
    
    /**
     * Builds and return secondary chart.
     * @return 
     */
    protected Node buildChart2()
    {
        tempPane = new Pane();
        return tempPane;
    }
    
    /**
     * Builds and returns primary Animation.
     * @return 
     */
    protected Node buildAnimation()
    {
        //animPane is the primary container for this animation
        animPane = new Pane();
        animPane.setPrefSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        
        //set height, width and position of rails with respect to the size of its container
        double height = CANVAS_HEIGHT;
        double width = CANVAS_WIDTH;
        double railSep = height*0.3;                                //the distance between both rails
        upperRail = new Rectangle(width*0.8, height*0.05, Color.SILVER);
        Rectangle lowerRail = new Rectangle(width*0.8, height*0.05, Color.SILVER);    
        upperRail.setX(width*0.1);                                  //sets the to the upper left corner of rectangle
        upperRail.setY((height*0.5) - (railSep*0.5) + height*0.05);
        lowerRail.setX(width*0.1);
        lowerRail.setY((height*0.5) + (railSep*0.5));
        
        //create and position armature with respect to the geometry and placement of rails
        armature = new Rectangle(height*0.05, railSep, Color.BLUE);
        armature.setX(width*0.2);
        armature.setY((height*0.5) - (railSep*0.5) + (height*0.05));
        armature.setOpacity(0.5);
        
        //create the projectile (iPad)
        projectile = new ImageView();
        projectile.setImage(new Image("file:src/images/iPad.png", true));
        projectile.setPreserveRatio(true);
        projectile.setSmooth(true);
        projectile.setCache(true);
        projectile.setFitWidth(railSep*0.6);
        projectile.setFitHeight(railSep*0.6);
        projectile.setRotate(90);
        
        //position the projectile with respect to the armature
        projectile.setX(armature.getX());
        projectile.setY(armature.getY() + armature.getHeight()/5);
        
        animPane.getChildren().addAll(upperRail, lowerRail, projectile, armature);
        
        return animPane;   
    }
    
    /**
    * Resets all toggle controls to default values.
    */
    protected void reset()
    {
        if(!(firstRun))
        {
            animation.stop();
            FadeTransition ft = new FadeTransition(Duration.millis(1000), projectile);
            ft.setToValue(1.0);
            TranslateTransition projTT = new TranslateTransition(Duration.millis(1000), projectile);
            projTT.setToX((CANVAS_WIDTH * 0.001));
            TranslateTransition armTT = new TranslateTransition(Duration.millis(1000), armature);
            armTT.setToX((CANVAS_WIDTH * 0.001));
            ParallelTransition parT = new ParallelTransition(ft, projTT, armTT);
            parT.play();
        
            lineChart.setAnimated(false);
            series.getData().clear();
            lineChart.getData().remove(series);
            lineChart.setAnimated(true);
        }
        
        current.setValue(DEFAULT_VALUE_CURRENT);
        mass.setValue(DEFAULT_VALUE_MASS);
        
        //enable the start button to be clicked
        actionButtonArray[0].setDisable(false);
        //disable the pause and continue buttons
        actionButtonArray[1].setDisable(true);
        actionButtonArray[2].setDisable(true);
    }
    
    /**
    * Starts the LorentzForceData animation.
    */
    protected void start()
    {
        //disable the start button
        actionButtonArray[0].setDisable(true);
        //enable the pause button
        actionButtonArray[1].setDisable(false);
        
        if(!(firstRun))
        {
            lineChart.setAnimated(false);
            series.getData().clear();
            lineChart.getData().remove(series);
            lineChart.setAnimated(true);
            lineChart.getData().add(series);
        }
        firstRun = false;
        cycleCount = 1;

        tempPane.getChildren().add(new Label("Total Time : " + Double.toString(totalTime.doubleValue()) + 
                "\n Final Velocity : " + Double.toString(finalVelocity.doubleValue())));
        
        // set lineChart values for xAxis and yAxis
        xAxis.setTickUnit((totalTime.doubleValue() + 0.05*totalTime.doubleValue()) / 5);
        xAxis.setUpperBound(totalTime.doubleValue() + 0.05*totalTime.doubleValue());
        yAxis.setTickUnit((finalVelocity.doubleValue() + 0.05*finalVelocity.doubleValue()) / 8.0);
        yAxis.setUpperBound(finalVelocity.doubleValue() + 0.05*finalVelocity.doubleValue());
        
        ParallelTransition parTransition = new ParallelTransition();
        // VERY IMPORTANT LINE
        animation = parTransition;
        
        //populating the series with data
        series.getData().add(new XYChart.Data(0, 0));
        
        //timeline for the chart
        Timeline chartTimeline = new Timeline();
        
        //totalCycleCount dictates how many line iterations will be added to the lineChart
        final int totalCycleCount = 100;
        chartTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(ANIMATION_DURATION / totalCycleCount), //ANIMATION_DURATION / totalCycleCount
            new EventHandler<ActionEvent>() 
            {
                @Override 
                public void handle(ActionEvent actionEvent) 
                {   
                    series.getData().add(new XYChart.Data((cycleCount*(totalTime.doubleValue()/totalCycleCount)), 
                            ((cycleCount*(totalTime.doubleValue()/totalCycleCount)*acceleration.doubleValue()))));
                    ++cycleCount;
                }
            }));
        chartTimeline.setCycleCount(totalCycleCount);
        
        //Translation for the armature
        TranslateTransition armTranslation = new TranslateTransition(Duration.millis(ANIMATION_DURATION), armature);
        armTranslation.setToX(upperRail.getX() + upperRail.getWidth() - armature.getX());
        armTranslation.setInterpolator(Interpolator.EASE_IN);
        
        //Translation (part 1) for the projectile, synchronous to armature's translation
        TranslateTransition projTranslation1 = new TranslateTransition(Duration.millis(ANIMATION_DURATION), projectile);
        projTranslation1.setToX(upperRail.getX() + upperRail.getWidth() - armature.getX());
        projTranslation1.setInterpolator(Interpolator.EASE_IN);
        
        //Translation (part 2) for the projectile, happens in parallel with FadeTransition
        TranslateTransition projTranslation2 = new TranslateTransition(Duration.millis(1000), projectile);
        projTranslation2.setByX(50);
        projTranslation2.setInterpolator(Interpolator.LINEAR);
        
        //Fade for the projectile, happens in parallel with part 2 of projectile Translation
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), projectile);
        fadeTransition.setToValue(0);
        
        //ParallelTransition to pair both the Fade and part 2 of the projectile Translation
        ParallelTransition projParTransition = new ParallelTransition();
        projParTransition.getChildren().addAll(projTranslation2, fadeTransition);
        
        //SequentialTransition, first being part 1 of projectile translation, followed
        //by the fade/translation (part 2) of projectile
        SequentialTransition projSeqTransition = new SequentialTransition();
        projSeqTransition.getChildren().addAll(projTranslation1, projParTransition);
        
        parTransition.getChildren().addAll(chartTimeline, armTranslation, projSeqTransition);
        parTransition.play();
        
        parTransition.setOnFinished(new EventHandler<ActionEvent>() 
            {
                @Override
                public void handle(ActionEvent e) 
                {
                    FadeTransition ft = new FadeTransition(Duration.millis(1000), projectile);
                    ft.setToValue(1.0);
                    TranslateTransition projTT = new TranslateTransition(Duration.millis(1000), projectile);
                    projTT.setToX((CANVAS_WIDTH * 0.001));
                    TranslateTransition armTT = new TranslateTransition(Duration.millis(1000), armature);
                    armTT.setToX((CANVAS_WIDTH * 0.001));
                    ParallelTransition parT = new ParallelTransition(ft, projTT, armTT);
                    parT.play();
                }
            });
    }
}