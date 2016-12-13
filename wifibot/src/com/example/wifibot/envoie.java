

package com.example.wifibot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.TimerTask;

import android.util.Log;


public class envoie extends TimerTask {

	private DataOutputStream dos = null;
	private DataInputStream dis = null;
	private boolean connected = false;
	byte[] dataArray = new byte[9];
	private final int RESPONSE_LENGTH = 21;
	
	private MainActivity context = null;
	byte[] rdata = new byte[RESPONSE_LENGTH];

	public envoie(MainActivity context){
		this.context = context;
	}

	/**
	 * 
	 * @parametre socket
	 * @parametre dos
	 * @parametre dis
	 */
	public void configure(DataOutputStream dos, DataInputStream dis ) {
		this.dos = dos;
		this.dis = dis;
		this.connected = true;
	}

	/**
	 * 
	 * @param dataArray
	 * @throws IOException
	 */
	private void writeCommand(byte[] data) {

		if(!connected){	return; }

		//demande d'écriture et  lit la réponse
		try {
			
			//vérifie la sécurité
			if(context.onSecurity) {
				//bloque l'avant
				if( (((int) dataArray[6] & 0x50) == 0x50) && 
						(rdata[11] > MainActivity.IR_LIMIT || rdata[3] > MainActivity.IR_LIMIT)){
					dataArray[2] = 0;
					dataArray[3] = 0;
					dataArray[4] = 0;
					dataArray[5] = 0;
				}
				
				if( (((int) dataArray[6] & 0x50) == 0x00) && 
						(rdata[12] > MainActivity.IR_LIMIT || rdata[4] > MainActivity.IR_LIMIT)){
					dataArray[2] = 0;
					dataArray[3] = 0;
					dataArray[4] = 0;
					dataArray[5] = 0;
				}
				
			}
			
			dos.write(data);
			dos.flush();

			String write_cmd = "";
			for(int i=0;i<9;i++) {
				write_cmd += String.format("%x", data[i]) + " ";
			}
			Log.d("WRITE",write_cmd);
			
			//lit les réponses
			dis.readFully(rdata);

			String read_cmd = "";
			for(int i=0;i<RESPONSE_LENGTH;i++) {
				read_cmd += String.format("%x", rdata[i]) + " ";
			}
			Log.d("READ",read_cmd);
			
			context.voltage = (short)(rdata[2] & 0xff);
			context.irDd = (short)(rdata[11] & 0xff);
			context.irDg = (short)(rdata[3] & 0xff);
			context.irAd = (short)(rdata[4] & 0xff);
			context.irAg = (short)(rdata[12] & 0xff);
			context.current = (short)(rdata[17] & 0xff);
			context.batteryState = (short)(rdata[18] & 0xff);
			context.handler.post(context.updateUI);
			

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param speed
	 * @throws IOException
	 */
	public void backward(int speed) {//bas
		dataArray[0] = (byte) 0xff;		//255
		dataArray[1] = (byte) 0x07;		//size

		dataArray[2] = (byte) speed;	//vitesse gauche 
		dataArray[3] = (byte) (speed>>8);
		dataArray[4] = (byte) speed;	//vitesse droite
		dataArray[5] = (byte) (speed>>8);
		dataArray[6] = (byte) 0x03;		//bas
		
		if(context.onMotorControl) {
			dataArray[6] = (byte) ((int)dataArray[6] | 0xa0);
		}
		
		if(MainActivity.MOTOR_CONTROL_PID_10) {
			dataArray[6] = (byte) ((int)dataArray[6] | 0x08);
		}

		CRC16 crc = new CRC16();
		for (int i=1; i<7; i++) {
			crc.update(dataArray[i]);
		}

		dataArray[7] = (byte) crc.getValue();		//crc16
		dataArray[8] = (byte) (crc.getValue()>>8);
	}


	/**
	 * 
	 * @param speed
	 * @throws IOException
	 */
	public void forward(int speed)  {//haut

		dataArray[0] = (byte) 0xff;
		dataArray[1] = (byte) 0x07;

		dataArray[2] = (byte) speed;
		dataArray[3] = (byte) (speed>>8);
		dataArray[4] = (byte) speed;
		dataArray[5] = (byte) (speed>>8);
		dataArray[6] = (byte) 0x53;

		if(context.onMotorControl) {
			dataArray[6] = (byte) ((int)dataArray[6] | 0xa0);
		}

		if(MainActivity.MOTOR_CONTROL_PID_10) {
			dataArray[6] = (byte) ((int)dataArray[6] | 0x08);
		}
		
		CRC16 crc = new CRC16();
		for (int i=1; i<7; i++) {
			crc.update(dataArray[i]);
		}

		dataArray[7] = (byte) crc.getValue();
		dataArray[8] = (byte) (crc.getValue()>>8);
	}


	public void nothing() {//rien

		dataArray[0] = (byte) 0xff;
		dataArray[1] = (byte) 0x07;

		dataArray[2] = (byte) 0x00;
		dataArray[3] = (byte) 0x00;
		dataArray[4] = (byte) 0x00;
		dataArray[5] = (byte) 0x00;
		dataArray[6] = (byte) 0x53;

		CRC16 crc = new CRC16();
		for (int i=1; i<7; i++) {
			crc.update(dataArray[i]);
		}

		dataArray[7] = (byte) crc.getValue();
		dataArray[8] = (byte) (crc.getValue()>>8);
	}

	/**
	 * 
	 * @param speed
	 * @throws IOException
	 */
	public void rotate(int speed, boolean clock)  {//rotation
		dataArray[0] = (byte) 0xff;
		dataArray[1] = (byte) 0x07;

		dataArray[2] = (byte) speed;
		dataArray[3] = (byte) (speed>>8);
		dataArray[4] = (byte) speed;
		dataArray[5] = (byte) (speed>>8);
		if(clock) {
			dataArray[6] = (byte) 0x43;
		}
		else {
			dataArray[6] = (byte) 0x13;
		}

		if(context.onMotorControl) {
			dataArray[6] = (byte) ((int)dataArray[6] | 0xa0);
		}
		
		if(MainActivity.MOTOR_CONTROL_PID_10) {
			dataArray[6] = (byte) ((int)dataArray[6] | 0x08);
		}
		
		CRC16 crc = new CRC16();
		for (int i=1; i<7; i++) {
			crc.update(dataArray[i]);
		}

		dataArray[7] = (byte) crc.getValue();
		dataArray[8] = (byte) (crc.getValue()>>8);
	}


	/**
	 * 
	 * @param speed
	 * @throws IOException
	 */
	public void direction(int speed, boolean right, boolean forward)  {//choix de la direction
		dataArray[0] = (byte) 0xff;	
		dataArray[1] = (byte) 0x07;

		if(!right)//si différent de droite
		{
			dataArray[2] = (byte) speed;
			dataArray[3] = (byte) (speed>>8);

			dataArray[4] = (byte) 0x00;
			dataArray[5] = (byte) 0x00;
			if(forward) {//si haut
				dataArray[6] = (byte) 0x43;
			}
			else {
				dataArray[6] = (byte) 0x03;
			}
		}
		else
		{
			dataArray[2] = (byte) 0x00;
			dataArray[3] = (byte) 0x00;

			dataArray[4] = (byte) speed;
			dataArray[5] = (byte) (speed>>8);
			if(forward) {//si haut
				dataArray[6] = (byte) 0x13;
			}
			else {
				dataArray[6] = (byte) 0x03;
			}
		}

		if(context.onMotorControl) {
			dataArray[6] = (byte) ((int)dataArray[6] | 0xa0);
		}

		if(MainActivity.MOTOR_CONTROL_PID_10) {
			dataArray[6] = (byte) ((int)dataArray[6] | 0x08);
		}
		
		CRC16 crc = new CRC16();
		for (int i=1; i<7; i++) {
			crc.update(dataArray[i]);
		}

		dataArray[7] = (byte) crc.getValue();
		dataArray[8] = (byte) (crc.getValue()>>8);
	}

	
	@Override
	public void run() {

		if(connected)
		{
			synchronized (dataArray) {
				writeCommand(dataArray);
			}
		}
	}
}