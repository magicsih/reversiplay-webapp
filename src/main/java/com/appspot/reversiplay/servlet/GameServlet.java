/**
 * 
 */
package com.appspot.reversiplay.servlet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
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
		resp.setContentType("text/plain");
		resp.getWriter().println("{ \"name\": \"World\" }");
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		LOG.info(req.getRequestURI());
		if("/game/new".equals(req.getRequestURI())) {
			resp.getOutputStream().write(ReversiGameComponent.newGame());
		} else if("/game/put".equals(req.getRequestURI())) {
			ServletInputStream inputStream = req.getInputStream();
			byte[] data = new byte[20];
			inputStream.read(data);
			
			List<String> binaryBoardState = new ArrayList<>(8);
			ByteBuffer byteBuffer = ByteBuffer.wrap(data);
			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

			ShortBuffer asShortBuffer = byteBuffer.asShortBuffer();
			
			for(int i=0 ; i<8 ;++i) {
				String binaryString = String.format("%16s", Integer.toBinaryString(asShortBuffer.get(i))).replace(' ', '0');
				LOG.info(binaryString);
				binaryBoardState.add(binaryString);
			}
			short who = asShortBuffer.get(8);
			short where = asShortBuffer.get(9);
			byte[] putResult = ReversiGameComponent.put(Piece.values()[who], binaryBoardState, where);
			resp.getOutputStream().write(putResult);
		} else {
			throw new RuntimeException("Unknown URL");
		}

		
	}
}
