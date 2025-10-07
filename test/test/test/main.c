//
//  main.c
//  test
//
//  Created by zhuwenwei on 2021/1/12.
//  Copyright © 2021 zhuwenwei. All rights reserved.
//

#include <stdio.h>


int main(int argc, const char * argv[]) {
    float R1,R2,U,I1,I2;

//    printf("Input three numbers R1,R2,U:\n");
//    scanf("%f,%f,%f",&R1,&R2,&U);
//    printf("R1=%f,R2=%f,U=%f\n",R1,R2,U);
//    I1=U/(R1+R2);
//    I2=U/((R1*R2)/(R1+R2));
//    printf("I1=%f,I2=%f\n",I1,I2);
//    int a = 3;
//    printf("%d\n", a);
    

//    for (int i = 0; i < width; i++){
//        for (int j = 0; j < height; j++){
//            printf("(%d, %d),", i, j);
//        }
//        printf("\n");
//    }
    
    
    int width = 9;//宽度
    int height = 9;//高度
    int index;//序号
    for (int i = width-1; i >= 0; i--){
        for (int j = height-1; j >= 0; j--){
            index = i*width+j;
            if (i != 0 && i != height-1 && j != 0 && j != width-1 && i%2 == 1 && j%2 == 1){
                printf("%d,", index);
                //设置颜色
                
            }
        }
        printf("\n");
    }
    
    
    
//    int width = 5;//宽度
//    int height = 5;//高度
//    int index;//序号
//    for (int i = width-1; i >= 0; i--){
//        for (int j = height-1; j >= 0; j--){
//            index = i*width+j;
//            if (i != 0 && i != height-1 && j != 0 && j != width-1 && i%2 == 1 && j%2 == 1){
//                //设置颜色
//
//            }
//        }
//    }
    
    
    
//    scanf("%f",&R1);
//    scanf("%f",&R2);
//    scanf("%f",&U);
//    scanf("%f,%f,%f",&R1,&R2,&U);
//    printf("R1=%f,R2=%f,U=%f",R1,R2,U);
//    I1=U/(R1+R2);
//    I2=U/((R1*R2)/(R1+R2));
//    printf("I1=%f,I2=%f",I1,I2);
//    getch();
    
    return 0;
}


