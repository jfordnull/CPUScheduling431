import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Comparator;

public class Main {
    public static void main(String[] args){

        //A process is defined as an array, [id][arrival][burst][priority]
        ArrayList<int[]> incomingProcesses = new ArrayList<>();

        //Priority queue with custom comparator:
        PriorityQueue<int[]> processQueue = new PriorityQueue<>((prc1, prc2) -> {
            //Compare burst times
            int comparison = Integer.compare(prc1[2], prc2[2]);
            //If burst times are the same, compare priority (arbitration)
            if (comparison == 0){return Integer.compare(prc1[3], prc2[3]);}
            return comparison;
        });

        //Parse process-table input file:
        try (BufferedReader br = new BufferedReader(new FileReader("processTable.txt"))){
            String line;
            while ((line = br.readLine()) != null){
                String[] values = line.split(" ");
                int[] prc = new int[4];
                for (int i = 0; i < 4; i++){prc[i] = Integer.parseInt(values[i]);}
                incomingProcesses.add(prc);
            }
        }
        catch(IOException e){
            System.err.println("Bad input");
            System.exit(1);
        }

        //Sort incoming processes by arrival time
        incomingProcesses.sort(Comparator.comparingInt(prc->prc[1]));
        //Total clock time is the sum of process burst times
        int totalClock = incomingProcesses.stream().mapToInt(prc->prc[2]).sum();

        sjf(new PriorityQueue<>(processQueue), new ArrayList<>(incomingProcesses), totalClock);
        srt(new PriorityQueue<>(processQueue), new ArrayList<>(incomingProcesses), totalClock);
    }

    private static void sjf(PriorityQueue<int[]> pq, ArrayList<int[]> ip, int timeEnd){
        System.out.println("Results for SJF:");
        boolean busy = false;
        int busyUntil = 0;
        double totalTurnaround = 0;
        int processCount = ip.size();
        //For loop simulates clock cycles
        for (int i = 0; i <= timeEnd; i++){
            int[] temp;
            //Handle process arrival
            if (!ip.isEmpty() && ip.get(0)[1] == i){
                pq.add(ip.get(0));
                ip.remove(0);
            }
            if (busy && i >= busyUntil){busy = false;}  //Check if CPU should be freed
            if (!pq.isEmpty() && !busy){
                temp = pq.poll();
                System.out.println("Process " + temp[0] + " completed at clock: " + (i + temp[2]));
                //TT = Completion - Arrival (same as burst + wait time)
                totalTurnaround += i + temp[2] - temp[1];
                busy = true;
                busyUntil = i + temp[2];    //Block CPU until process would be complete
            }
        }
        System.out.println("Average turnaround time: " + (totalTurnaround/processCount) + " ms");
    }

    private static void srt(PriorityQueue<int[]> pq, ArrayList<int[]> ip, int timeEnd){
        System.out.println("Results for SRT:");
        double totalTurnaround = 0;
        int processCount = ip.size();
        //For loop simulates clock cycles
        for (int i = 0; i <= timeEnd; i++){
            int[] temp;
            //Handle process arrival
            if (!ip.isEmpty() && ip.get(0)[1] == i){
                pq.add(ip.get(0));
                ip.remove(0);
            }
            //Decrement process burst at head of queue by one
            if (!pq.isEmpty()){
                temp = pq.poll();
                temp[2]--;
                //If process is finished:
                if (temp[2] == 0){
                    System.out.println("Process " + temp[0] + " completed at clock: " + (i + 1));
                    //TT = Completion - Arrival (same as burst + wait time)
                    totalTurnaround += i + 1 - temp[1];
                }
                //Else add decremented process back into queue to be sorted
                else{pq.add(temp);}
            }
        }
        System.out.println("Average turnaround time: " + (totalTurnaround/processCount) + " ms");
    }
}