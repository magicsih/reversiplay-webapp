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

import com.appspot.reversiplay.game.ReversiGame;
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
		resp.setContentType("text/plain");
		resp.getWriter().println("{ \"name\": \"World\" }");
	}

	private static final int BOARD_SIZE = 8;
	private static final int BITS_PER_BYTE = 8;
	private static final int BIN_SIZE_OF_BOARD = BOARD_SIZE * BOARD_SIZE * 2;
	private static final int BYTE_SIZE_OF_BOARD = BIN_SIZE_OF_BOARD / BITS_PER_BYTE;

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ReversiGame game = new ReversiGame(8);
		//        resp.setContentType("appication/json");
		LOG.info(game.toString());
		if("/game/new".equals(req.getRequestURI())) {
//			ByteBuffer boardStateBuffer = ByteBuffer.allocate(BYTE_SIZE_OF_BOARD);
//			boardStateBuffer.order(ByteOrder.LITTLE_ENDIAN);
//			for(int r=0;r<BOARD_SIZE;++r){
//				StringBuilder sb = new StringBuilder();
//				for(int c=0; c<BOARD_SIZE;++c) {
//					int cell = r * BOARD_SIZE + c;
//					
//					if("B".equals(game.getBoard().get(cell))){
//						sb.append("01");
//					} else if("W".equals(game.getBoard().get(cell))){
//						sb.append("10");
//					} else if("V".equals(game.getBoard().get(cell))){
//						sb.append("11");
//					} else {
//						sb.append("00");
//					}
//				}
//				String string = sb.toString();
//				
//				short v = Short.parseShort(string,2);
//				System.out.println(string + "-" + v);
//				
//				boardStateBuffer.putShort(v);
//			}
//
//			resp.getOutputStream().write(boardStateBuffer.array());
			resp.getOutputStream().write(ReversiGameComponent.newGame());
		} else if("/game/put".equals(req.getRequestURI())) {
			ServletInputStream inputStream = req.getInputStream();
			byte[] data = new byte[16];
			inputStream.read(data);
			
			List<String> lines = new ArrayList<>(8);
			ByteBuffer byteBuffer = ByteBuffer.wrap(data);
			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
			short[] values = new short[8];
			byteBuffer.asShortBuffer().get(values);
			for(short s: values) {
				String binaryString = String.format("%16s", Integer.toBinaryString(s)).replace(' ', '0');
				System.out.println(binaryString); 
				lines.add(binaryString);
			}
		} else {
			throw new RuntimeException("Unknown URL");
		}

		
	}
}
