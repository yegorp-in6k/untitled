package com.company;

import com.prudsys.pdm.Core.MiningAlgorithmSpecification;
import com.prudsys.pdm.Core.MiningDataSpecification;
import com.prudsys.pdm.Core.MiningException;
import com.prudsys.pdm.Core.MiningModel;
import com.prudsys.pdm.Input.MiningInputStream;
import com.prudsys.pdm.Input.Records.Arff.MiningArffStream;
import com.prudsys.pdm.Models.Clustering.Cluster;
import com.prudsys.pdm.Models.Clustering.ClusteringSettings;
import com.prudsys.pdm.Models.Clustering.CDBased.Algorithms.KMeans.KMeans;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class App {

    public static void main(String[] args) throws MiningException, ClassNotFoundException, IllegalAccessException, InstantiationException, IOException {
        MiningInputStream inputData = new MiningArffStream("C:\\Xelopes6.5_openSource\\data\\arff\\transact.arff");
        MiningDataSpecification metaData = inputData.getMetaData();
        //MiningAlgorithm miningAlgorithm ;
        ClusteringSettings miningSettings = new ClusteringSettings();
        miningSettings.setDataSpecification(metaData);
        miningSettings.setClusterIdAttributeName("ItemID");
        FileReader reader = new FileReader("C:\\Xelopes6.5_openSource\\data\\pmml\\ClusteringModel.xml");
        MiningAlgorithmSpecification miningAlgorithmSpecification = MiningAlgorithmSpecification.getMiningAlgorithmSpecification();
        String className = miningAlgorithmSpecification.getClassname();
        if( className == null )
            throw new MiningException( "className attribute expected." );
        Class algorithmClass = Class.forName( className );
        Object algorithm = algorithmClass.newInstance();
        MiningModel model = new MyClusteringAlgorithm();
        model.readPmml(reader);
        miningSettings.verifySettings();
        System.out.println("number of clusters: " +
                model.getNumberOfClusters());
        Cluster[] clust = model.getClusters();
        for (int i = 0; i < clust.length; i++)
            System.out.println("Clust["+i+"]: " + clust[i].toString() );
        FileWriter writer = new FileWriter("C:\\Xelopes6.5_openSource\\data\\arff\\transact.txt");
        model.writePlainText(writer);
    }

}
