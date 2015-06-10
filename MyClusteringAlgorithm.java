package com.company;

import com.prudsys.pdm.Core.MiningDataSpecification;
import com.prudsys.pdm.Core.MiningException;
import com.prudsys.pdm.Input.MiningInputStream;
import com.prudsys.pdm.Input.MiningVector;
import com.prudsys.pdm.Input.Records.Arff.MiningArffStream;
import com.prudsys.pdm.Models.Clustering.CDBased.Algorithms.KMeans.Log;
import com.prudsys.pdm.Models.Clustering.CDBased.CDBasedClusteringMiningModel;
import com.prudsys.pdm.Models.Clustering.Cluster;
import com.prudsys.pdm.Models.Clustering.ClusteringSettings;
import com.prudsys.pdm.Models.Clustering.Distance;

import java.util.Random;

/**
 * Created by Егор on 04.06.2015.
 */
public class MyClusteringAlgorithm extends CDBasedClusteringMiningModel {

    MiningInputStream miningInputStream = new MiningArffStream("C:\\Xelopes6.5_openSource\\data\\arff\\transact.arff");
    MiningDataSpecification metaData = miningInputStream.getMetaData();
    //MiningAlgorithm miningAlgorithm ;
    ClusteringSettings miningSettings = new ClusteringSettings();
    miningSettings.setDataSpecification(metaData);

    public MyClusteringAlgorithm() {

    }


    protected void runAlgorithm(){
        int numbAtt = metaData.getAttributesNumber();
        int numbVec = 0;

        // Get minimum and maximum of attributes:
        double[] minArr = new double[ numbAtt ];
        double[] maxArr = new double[ numbAtt ];
        for (int i = 0; i < numbAtt; i++) {
            minArr[i] = 0.0;
            maxArr[i] = 0.0;
        };
        while (miningInputStream.next()) {
            MiningVector vec = miningInputStream.read();
            for (int i = 0; i < numbAtt; i++) {
                if (vec.getValue(i) < minArr[i])
                    minArr[i] = vec.getValue(i);
                if (vec.getValue(i) > maxArr[i])
                    maxArr[i] = vec.getValue(i);
            };
            numbVec = numbVec + 1;
        };
        distance.setMinAtt( minArr );
        distance.setMaxAtt( maxArr );

        // Create array of clusters:
        clusters = new Cluster[ numberOfClusters ];
        for (int i = 0; i < numberOfClusters; i++) {
            clusters[i] = new Cluster();
            clusters[i].setName("clust" + String.valueOf(i));
        }

        // Find numbOfClusters random vectors:
        getCluster(numbVec);

        // Iterations:
        numberOfIterations = 0;
        boolean converged  = false;
        while (! converged && numberOfIterations < maxNumberOfIterations) {
            Log.getLogger().debug("iter: " + (numberOfIterations+1));
            converged = true;

            // Find nearest cluster for all vectors:
            for (int i = 0; i < numbVec; i++) {
                int nC = clusterVector( miningInputStream.read(i) );
                if (nC != clusterAssignments[i]) {
                    clusterAssignments[i] = nC;
                    converged             = false;
                };
            };

            // Find new center vectors:
            for (int i = 0; i < numberOfClusters; i++) {
                double[] nullVal     = new double[ numbAtt ];
                MiningVector nullVec = new MiningVector( nullVal );
                nullVec.setMetaData( metaData );
                clusters[i].setCenterVec( nullVec );
            };
            int[] cardClusters = new int[numberOfClusters];
            for (int i = 0; i < numbVec; i++) {
                MiningVector vec = miningInputStream.read(i);
                int index        = clusterAssignments[i];
                for (int j = 0; j < numbAtt; j++) {
                    double val = clusters[index].getCenterVec().getValue(j);
                    val        = val + vec.getValue(j);
                    clusters[index].getCenterVec().setValue(j, val);
                };
                cardClusters[index] = cardClusters[index] + 1;
            };
            for (int i = 0; i < numberOfClusters; i++) {
                for (int j = 0; j < numbAtt; j++) {
                    double val = clusters[i].getCenterVec().getValue(j);
                    int card   = cardClusters[i];
                    if (card == 0)
                        card = 1;
                    val        = val / card;
                    clusters[i].getCenterVec().setValue(j, val);
                };
            };
            numberOfIterations = numberOfIterations + 1;
        };

    }

    private void getCluster(int numbVec) throws MiningException {
        clusterAssignments = new int[numbVec];
        boolean selected[] = new boolean[numbVec];
        Random rand        = new Random(10);
        for (int i = 0; i < numberOfClusters; i++) {
            int index = 0;
            do {
                index = rand.nextInt(numbVec);
            }
            while (selected[index]);

            // Add center vector to cluster array:
            MiningVector vec = miningInputStream.read(index);
            clusters[i].setCenterVec( vec );

            selected[index] = true;
        }
        ;
    }

    public Distance getDistances(){

        return distance;
    }


}
