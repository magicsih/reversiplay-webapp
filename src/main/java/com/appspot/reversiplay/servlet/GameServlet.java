/**
 * 
 */
package com.appspot.reversiplay.servlet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.appspot.reversiplay.game.Piece;
import com.appspot.reversiplay.game.ReversiGameComponent;

/**
 * @author seoil
 *
 */
public class GameServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(GameServlet.class.getSimpleName());

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//		resp.setContentType("text/plain");
//		resp.getWriter().println("{ \"name\": \"World\" }");
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		LOG.info(req.getRequestURI());
		if("/game/new".equals(req.getRequestURI())) {
			resp.getOutputStream().write(ReversiGameComponent.newGame());
		} else if("/game/bestmove".equals(req.getRequestURI())) {
			ServletInputStream inputStream = req.getInputStream();
			byte[] data = new byte[20];
			inputStream.read(data);
			
			List<String> binaryBoardState = new ArrayList<>(8);
			ByteBuffer byteBuffer = ByteBuffer.wrap(data);
			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

			byte[] array = byteBuffer.array();
			
			for(int i=0; i < 16 ; i=i+2) {
				byte[] integerByte = new byte[4];
				integerByte[0] = array[i];
				integerByte[1] = array[i+1];
				integerByte[2] = (byte)0x0;
				integerByte[3] = (byte)0x0;
				ByteBuffer intBuf = ByteBuffer.wrap(integerByte).order(ByteOrder.LITTLE_ENDIAN);
				String binaryString = String.format("%16s", Integer.toBinaryString(intBuf.getInt())).replace(' ', '0');
//				System.out.println(binaryString);
				binaryBoardState.add(binaryString);
			}
			
			byte[] whoBytes = new byte[2];
			whoBytes[0] = array[16];
			whoBytes[1] = array[17];
			
			short who = ByteBuffer.wrap(whoBytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
			
			byte[] depthBytes = new byte[2];
			depthBytes[0] = array[18];
			depthBytes[1] = array[19];
			short depth = ByteBuffer.wrap(depthBytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
			
			byte[] bestMoveResult = ReversiGameComponent.getBestMove(Piece.values()[who], binaryBoardState, depth);
			resp.getOutputStream().write(bestMoveResult);
			
		} else if("/game/put".equals(req.getRequestURI())) {
			ServletInputStream inputStream = req.getInputStream();
			byte[] data = new byte[20];
			inputStream.read(data);
			
			List<String> binaryBoardState = new ArrayList<>(8);
			ByteBuffer byteBuffer = ByteBuffer.wrap(data);
			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

			byte[] array = byteBuffer.array();
			
			for(int i=0; i < 16 ; i=i+2) {
				byte[] integerByte = new byte[4];
				integerByte[0] = array[i];
				integerByte[1] = array[i+1];
				integerByte[2] = (byte)0x0;
				integerByte[3] = (byte)0x0;
				ByteBuffer intBuf = ByteBuffer.wrap(integerByte).order(ByteOrder.LITTLE_ENDIAN);
				String binaryString = String.format("%16s", Integer.toBinaryString(intBuf.getInt())).replace(' ', '0');
//				System.out.println(binaryString);
				binaryBoardState.add(binaryString);
			}
			
			byte[] whoBytes = new byte[2];
			whoBytes[0] = array[16];
			whoBytes[1] = array[17];
			
			short who = ByteBuffer.wrap(whoBytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
			
			byte[] whereBytes = new byte[2];
			whereBytes[0] = array[18];
			whereBytes[1] = array[19];
			short where = ByteBuffer.wrap(whereBytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
			
			byte[] putResult = ReversiGameComponent.put(Piece.values()[who], binaryBoardState, where);
			resp.getOutputStream().write(putResult);
		} else {
			throw new RuntimeException("Unknown URL");
		}

		
	}
}
