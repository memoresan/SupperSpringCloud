package entity.java;

import java.util.Arrays;

public class leecode {

    public static void main(String[] agrs){
        int[] nums = new int[]{7,5,6,4};
        //getLeastNumbers(nums,2);
        mergeSort(nums);
        System.out.println(nums);
    }

    public static int[] getLeastNumbers(int[] arr, int k) {
        buildMaxHeap(arr);
        int cn = arr.length -1;
        if(k==1){
            return new int[]{arr[0]};
        }
        while(cn >=0){
            swap(arr,0,cn);
            maxHeapify(arr,0,cn);
            if(cn == arr.length - k){
                break;
            }
            cn--;
        }
        return Arrays.copyOfRange(arr,arr.length-k,arr.length);
    }

    private static void buildMaxHeap(int[] arr){
        for(int i=arr.length/2-1;i>=0;i--){
            maxHeapify(arr,i,arr.length);
        }
    }

    private static void maxHeapify(int[] arr, int i, int size){
        int left = i * 2 + 1;
        int right = left + 1;
        int minIndex=i;
        if(left < size && arr[minIndex] > arr[left]){
            minIndex = left ;
        }
        if(right < size && arr[minIndex] > arr[right]){
            minIndex = right ;
        }
        if(minIndex != i){
            swap(arr,i,minIndex);
            maxHeapify(arr,minIndex,size);
        }
    }
    private static void swap(int[] arr, int dest, int target){
        if(dest == target){
            return;
        }
        arr[dest] = arr[dest] ^ arr[target];
        arr[target] = arr[dest] ^ arr[target];
        arr[dest] = arr[dest] ^ arr[target];
    }

    public static void mergeSort(int[] arr) {
        if (arr.length == 0) return;
        mergeSort(arr, 0, arr.length - 1);
    }

    private static void mergeSort(int[] arr, int start, int end) {
        //只剩一个数字就停止拆分
        if(start == end) return ;
        int middle = (start+end)/2;
        mergeSort(arr,start,middle);
        mergeSort(arr,middle+1,end);
        merge(arr,start,end);


    }

    private static void merge(int[] arr, int start, int end) {
        int end1 = (start+end)/2;
        int start2 = end1+1;
        int index1 = start;
        int index2 = start2;
        while(index1 <= end1 && index2 <= end){
            if(arr[index1] <= arr[index2]){
                index1++;
            }else {
                int value = arr[index2];
                int index = index2;
                while(index > index1){
                    arr[index] = arr[index-1];
                    index--;
                }
                arr[index] = value;
                index1++;
                index2++;
                end1++;
            }
        }


    }


    public static void mergeSort1(int[] arr) {
        if (arr.length == 0) return;
        mergeSort1(arr, 0, arr.length - 1);
    }

    // 对 arr 的 [start, end] 区间归并排序
    private static void mergeSort1(int[] arr, int start, int end) {
        // 只剩下一个数字，停止拆分
        if (start == end) return;
        int middle = (start + end) / 2;
        // 拆分左边区域
        mergeSort1(arr, start, middle);
        // 拆分右边区域
        mergeSort1(arr, middle + 1, end);
        // 合并左右区域
        merge1(arr, start, end);
    }

    // 将 arr 的 [start, middle] 和 [middle + 1, end] 区间合并
    private static void merge1(int[] arr, int start, int end) {
        int end1 = (start + end) / 2;
        int start2 = end1 + 1;
        // 用来遍历数组的指针
        int index1 = start;
        int index2 = start2;
        while (index1 <= end1 && index2 <= end) {
            if (arr[index1] <= arr[index2]) {
                index1++;
            } else {
                // 右边区域的这个数字比左边区域的数字小，于是它站了起来
                int value = arr[index2];
                int index = index2;
                // 前面的数字不断地后移
                while (index > index1) {
                    arr[index] = arr[index - 1];
                    index--;
                }
                // 这个数字坐到 index1 所在的位置上
                arr[index] = value;
                // 更新所有下标，使其前进一格
                index1++;
                index2++;
                end1++;
            }
        }
    }

}
