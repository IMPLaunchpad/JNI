#include<jni.h>


#include <termios.h>
#include <error.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/mman.h>
#include <fcntl.h>
#define TEXTLCD_BASE        0xbc
#define TEXTLCD_COMMAND_SET _IOW(TEXTLCD_BASE,0,int)
#define TEXTLCD_FUNCTION_SET _IOW(TEXTLCD_BASE,1,int)
#define TEXTLCD_DISPLAY_SET _IOW(TEXTLCD_BASE,2,int)
#define TEXTLCD_CURSOR_SHIFT _IOW(TEXTLCD_BASE,3,int)
#define TEXTLCD_ENTRY_MODE_SET _IOW(TEXTLCD_BASE,4,int)
#define TEXTLCD_RETURN_HOME _IOW(TEXTLCD_BASE,5,int)
#define TEXTLCD_CLEAR _IOW(TEXTLCD_BASE,6,int)
#define TEXTLCD_DD_ADDRESS _IOW(TEXTLCD_BASE,7,int)
#define TEXTLCD_WRITE_BYTE _IOW(TEXTLCD_BASE,8,int)

 //////////////
#define FULL_LED1 9
#define FULL_LED2 8
#define FULL_LED3 7
#define FULL_LED4 6
#define ALL_LED 5




 ////////
 struct strcommand_variable {
    char rows;
    char nfonts;
    char display_enable;
    char cursor_enable;

    char nblink;
    char set_screen;
    char set_rightshit;
    char increase;
    char nshift;
    char pos;
    char command;
    char strlength;
    char buf[16];
};

static struct strcommand_variable strcommand;
static int initialized = 0;

void initialize(){
    if(!initialized){
        strcommand.rows = 0;
        strcommand.nfonts = 0;
        strcommand.display_enable = 1;
        strcommand.cursor_enable = 0;
        strcommand.nblink = 0;
        strcommand.set_screen = 0;
        strcommand.set_rightshit = 1;
        strcommand.increase = 1;
        strcommand.nshift = 0;
        strcommand.pos = 10;
        strcommand.command = 1;
        strcommand.strlength = 16;
        initialized = 1;
    }
}

int TextLCDloctol(int cmd, char * buf){
    int fd,ret,i;

    fd = open("/dev/fpga_textlcd", O_WRONLY | O_NDELAY);
    if(fd<0)return -errno;

    if(cmd == TEXTLCD_WRITE_BYTE) {
        ioctl(fd, TEXTLCD_DD_ADDRESS, &strcommand, 32);
        for (i = 0; i < strlen(buf); i++) {
            strcommand.buf[0] = buf[i];
            ret = ioctl(fd, cmd, &strcommand, 32);
        }
    } else {
        ret = ioctl(fd, cmd, &strcommand, 32);
    }

    close(fd);

    return ret;
}

 JNIEXPORT jint JNICALL
        Java_com_example_myapplication_MainActivity_TextLCDOut(
                JNIEnv* env,
                jobject abc,
                jstring data0,
                jstring data1
                ) {

     jboolean iscopy;
     char *buf0, *buf1;
     int fd, ret;

     fd = open("/dev/fpga_textlcd", O_WRONLY | O_NDELAY);
     if (fd < 0) return -errno;

     initialize();

     buf0 = (char *) (*env)->GetStringUTFChars(env, data0, &iscopy);
     buf1 = (char *) (*env)->GetStringUTFChars(env, data1, &iscopy);

     strcommand.pos = 0;
     ioctl(fd, TEXTLCD_DD_ADDRESS, &strcommand, 32);
     ret = write(fd, buf0, strlen(buf0));

     strcommand.pos = 40;
     ioctl(fd, TEXTLCD_DD_ADDRESS, &strcommand, 32);
     ret = write(fd, buf1, strlen(buf1));

     close(fd);

     return ret;

 }


JNIEXPORT jint JNICALL
Java_com_example_myapplication_MainActivity_IOCtlClear(
        JNIEnv* env,
        jobject abc
) {
    initialize();
    return TextLCDloctol(TEXTLCD_CLEAR, NULL);
}





 ///////////////////////////////////
 /////////////////FULL LED/////////
 //////////////////////////////////////////


JNIEXPORT jint JNICALL
Java_com_example_myapplication_MainActivity_FLEDControl(
        JNIEnv* env,
        jobject abc,
        jint led_num,
        jint val1,
        jint val2,
        jint val3
) {
    int fd, ret;
    char buf[3];
    fd = open("/dev/fpga_fullcolorled", O_WRONLY);
    if (fd < 0){
        return -errno;
    }
    ret = (int)led_num;
    switch (ret) {
        case FULL_LED1:
            ioctl(fd,FULL_LED1);
            break;
        case FULL_LED2:
            ioctl(fd,FULL_LED2);
            break;
        case FULL_LED3:
            ioctl(fd,FULL_LED3);
            break;
        case FULL_LED4:
            ioctl(fd,FULL_LED4);
            break;
        case ALL_LED:
            ioctl(fd,ALL_LED);
            break;
    }
    buf[0] = val1;
    buf[1] = val2;
    buf[2] = val3;

    write(fd, buf, 3);

    close(fd);
    return ret;
}




//     short value = 0;
//     unsigned short *addr_fpga;
//     unsigned short *keypad_row_addr, *keypad_col_addr, *piezo_addr;
//
//     int fd;
//
//     int i, quit = 1;
//
//     fd = open("/dev/mem", O_RDWR | O_SYNC);
//     if (fd == -1) {
//         perror("mem open fail\n");
//         return errno;
//     }
//         //exit(1);
//
//     addr_fpga = (unsigned short *)mmap(NULL, 4096, PROT_WRITE | PROT_READ, MAP_SHARED, fd, FPGA_BASEADDRESS);
//     keypad_col_addr = addr_fpga + KEY_COL_OFFSET / sizeof(unsigned short);
//     keypad_row_addr = addr_fpga + KEY_ROW_OFFSET / sizeof(unsigned short);
//     piezo_addr = addr_fpga + PIEZO_OFFSET / sizeof(unsigned short);
//
//     if (*keypad_row_addr == (unsigned short) -1 || *keypad_col_addr == (unsigned short) -1) {
//         close(fd);
//         printf("mmap error\n");
//         exit(1);
//     }
//     printf("-Keypad\n");
//     printf("press the key button!\n");
//     printf("press the key 0x16 to exit!\n");
//     while (quit) {
//         *keypad_row_addr = 0x01;
//         usleep(1000);
//         value = (*keypad_col_addr & 0x0f);
//         *keypad_row_addr = 0x00;
//         switch (value) {
//             case 0x01 :
//                 value = 0x01;
//                 break;
//             case 0x02 :
//                 value = 0x02;
//                 break;
//             case 0x04 :
//                 value = 0x03;
//                 break;
//             case 0x08 :
//                 value = 0x04;
//                 break;
//         }
//         if (value != 0x00)
//             goto stop_poll;
//         *keypad_row_addr = 0x02;
//         for (i = 0; i < 2000; i++);
//         value = value | (*keypad_col_addr & 0x0f);
//         *keypad_row_addr = 0x00;
//         switch (value) {
//             case 0x01 :
//                 value = 0x05;
//                 break;
//             case 0x02 :
//                 value = 0x06;
//                 break;
//             case 0x04 :
//                 value = 0x07;
//                 break;
//             case 0x08 :
//                 value = 0x08;
//                 break;
//         }
//         if (value != 0x00)
//             goto stop_poll;
//         *keypad_row_addr = 0x04;
//         for (i = 0; i < 2000; i++);
//         value = value | (*keypad_col_addr & 0x0f);
//         *keypad_row_addr = 0x00;
//         switch (value) {
//             case 0x01 :
//                 value = 0x09;
//                 break;
//             case 0x02 :
//                 value = 0x0a;
//                 break;
//             case 0x04 :
//                 value = 0x0b;
//                 break;
//             case 0x08 :
//                 value = 0x0c;
//                 break;
//         }
//         if (value != 0x00)
//             goto stop_poll;
//         *keypad_row_addr = 0x08;
//         for (i = 0; i < 2000; i++);
//         value = value | (*keypad_col_addr & 0x0f);
//         *keypad_row_addr = 0x00;
//         switch (value) {
//             case 0x01 :
//                 value = 0x0d;
//                 break;
//             case 0x02 :
//                 value = 0x0e;
//                 break;
//             case 0x04 :
//                 value = 0x0f;
//                 break;
//             case 0x08 :
//                 value = 0x10;
//                 break;
//         }
//         stop_poll:
//         if (value > 0) {
//             printf("\n pressed key = %02d\n", value);
//             *piezo_addr = 0x1;
//             usleep(50000);
//             *piezo_addr = 0x0;
//             //return value;
//             //break;
//         } else *keypad_row_addr = 0x00;
//
//
//         for (i = 0; i < 4000000; i++);
//         if (value == 16) {
//
//             printf("\nExitProgram!! (key = %02d)\n\n", value);
//
//             *piezo_addr = 0x1;
//
//             usleep(150000);
//
//             *piezo_addr = 0x0;
//
//             quit = 0;
//
//         }
//
//
//         munmap(addr_fpga, 4096);
//
//         close(fd);
//
//     }
//
//     return value;
//}

