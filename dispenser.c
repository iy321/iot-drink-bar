#include <stdio.h>
#include <stdlib.h>
#include <wiringPi.h>
#include <unistd.h>
#include <signal.h>

// GPIO ピン番号
#define FLAVOR1_PIN         17  // 1つ目のフレーバーのin1 ピン番号
#define FLAVOR2_PIN         27  // 2つ目のフレーバーのin1 ピン番号
#define FLAVOR3_PIN         22  // 3つ目のフレーバーのin1 ピン番号
int flavor_pin; // フレーバーのピン番号

// 注ぐ時間
#define MAX_POURING_TIME    15
#define MIN_POURING_TIME    0

// フレーバー
enum flavor {
	FLAVOR1 = 0,
	FLAVOR2,
	FLAVOR3,
};

// エラー時に呼ばれるハンドラ
void error_handler(int sig) {
	// 注ぐのを止める
	digitalWrite(flavor_pin, 0);
}

int main(int argc, char const *argv[]) {
	if (argc != 3) {
		printf("--- Raspberry Pi beverage dispenser controller ---\n");
			printf("Team Niku no Ban-nin (Hayato Kohara, 
		Yu Ichikawa, Yuma Ichinomiya)\n");
				printf("\n");
				printf("Usage: dispenser flavor_number pouring_time\n");
				printf("[ Arguments ]\n");
				printf("%-15s : the drink's flavor number (0-2)\n", "flavor_number");
				printf("%-15s : the time for pouring a drink (sec)\n", "pouring_time");
				exit(0);
			}
			
			// wiringPiのセットアップ
			if (wiringPiSetupGpio() == -1) {
				perror("setup");
				exit(1);
			}
			
			// ピン番号の設定
			switch ((enum flavor)atoi(argv[1])) {
			  case FLAVOR1:
				flavor_pin = FLAVOR1_PIN;
				break;
			  case FLAVOR2:
				flavor_pin = FLAVOR2_PIN;
				break;
			  case FLAVOR3:
				flavor_pin = FLAVOR3_PIN;
				break;
			  default:
				perror("flavor_number");
				exit(1);
			}
			pinMode(flavor_pin, OUTPUT);
			
			// 注ぐ秒数の設定
			int pouring_time = atoi(argv[2]);
			if (pouring_time > MAX_POURING_TIME || 
				pouring_time < MIN_POURING_TIME) {
				// 多すぎたらやめとく
				perror("pouring_time");
				exit(1);
			}
			
			// 垂れ流しを防ぐためにエラーハンドラを設定する
			if (signal(SIGABRT, error_handler) == SIG_ERR &&
				signal(SIGTERM, error_handler) == SIG_ERR &&
				signal(SIGINT,  error_handler) == SIG_ERR) {
				exit(1);
			}
			
			// 指定した秒数注ぐ
			digitalWrite(flavor_pin, 1);
			sleep(pouring_time);
			digitalWrite(flavor_pin, 0);
			
			return 0;
			
		}