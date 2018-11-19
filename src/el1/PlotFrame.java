package el1;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;

import javax.swing.*;
import java.awt.*;

public class PlotFrame extends JFrame {
    public PlotFrame(double[][] points) throws HeadlessException {
        super("Эллиптическая кривая");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        DefaultXYDataset dataset = new DefaultXYDataset();
        dataset.addSeries("", points);
        JFreeChart chart = ChartFactory.createScatterPlot("", "", "",dataset,PlotOrientation.VERTICAL,false,false,false);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new ChartPanel(chart));
        pack();
        setVisible(true);
    }
}