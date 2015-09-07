package jacz.util.AI.resource_distribution.test;

import jacz.util.AI.resource_distribution.AdaptiveResourceDistributor2;

import java.util.ArrayList;
import java.util.List;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 1/07/12<br>
 * Last Modified: 1/07/12
 */
public class TestAdaptiveResourceDistributor {

    public static void main(String args[]) {

        List<Float> stakeholdersPreviousToLastAssignedResources = new ArrayList<Float>();
        add(stakeholdersPreviousToLastAssignedResources, 30, 20, 15);
        List<Float> stakeholdersLastAssignedResources = new ArrayList<Float>();
        add(stakeholdersLastAssignedResources, 35, 25, 10);
        List<Float> stakeholdersLastConsumedResources = new ArrayList<Float>();
        add(stakeholdersLastConsumedResources, 40, 30, 15);

//        List<Float> stakeholdersPreviousToLastAssignedResources = new ArrayList<Float>();
//        add(stakeholdersPreviousToLastAssignedResources, 0, 0, 0);
//        List<Float> stakeholdersLastAssignedResources = new ArrayList<Float>();
//        add(stakeholdersLastAssignedResources, 0, 0, 0);
//        List<Float> stakeholdersLastConsumedResources = new ArrayList<Float>();
//        add(stakeholdersLastConsumedResources, 0, 0, 0);

        List<Float> wishedDistribution = new ArrayList<>();
        add(wishedDistribution, 10, 10, 5);
        List<Float> stakeholdersMaxDesiredResources = new ArrayList<Float>();
        stakeholdersMaxDesiredResources.add(null);
        stakeholdersMaxDesiredResources.add(null);
        stakeholdersMaxDesiredResources.add(null);
        float threshold = 0.95f;
        float loweringPercentage = 0.3f;

        Float max = 300f;

        AdaptiveResourceDistributor2.Result result = AdaptiveResourceDistributor2.distributeResources(max, stakeholdersPreviousToLastAssignedResources, stakeholdersLastAssignedResources, stakeholdersLastConsumedResources, wishedDistribution, stakeholdersMaxDesiredResources, threshold, loweringPercentage);

        System.out.println("END");
    }


    private static void add(List<Float> list, float f1, float f2, float f3) {
        list.add(f1);
        list.add(f2);
        list.add(f3);
    }
}
