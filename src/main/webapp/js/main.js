var gameBoard = null;
var relayingSubscription =null;
const myTurn = 0; // Current Black Only

gameBoard = new GameBoard("gameBoard", 8, {
	onPiecePut : function(turn, board, row, col) {
		console.log(board);

		var num = new Uint16Array(10);

		for(var i=0;i<8;++i) {
			var binaryString = "";
			for(var j=0;j<8;++j) {
				var c = board[i*8+j];
				if(c === "B") {
					binaryString += "01";
				} else if (c=== "W") {
					binaryString += "10";
				} else {
					binaryString += "00";
				}
			}
			console.log(binaryString);
			num[i] = parseInt(binaryString, 2);
		}
		num[8] = turn;
		num[9] = row *8 + col;
		console.log(num);
		console.log("Put piece at (row,col): " + row + "," + col);

		var xhr = new XMLHttpRequest();
		xhr.open('POST', '/game/put', true);
		xhr.responseType = 'arraybuffer';


		xhr.onload = function(e) {
			if (this.status == 200) {
				onBoardDataReceived(xhr.response);
			}
		};

		xhr.send(num);
	}
});

window.addEventListener('resize', resizeCanvas, false);

function resizeCanvas() {
	var canvas = document.getElementById('gameBoard');
	var baseLength = ((window.innerWidth-20) > (window.innerHeight - 100)) ? (window.innerHeight-100) : (window.innerWidth-20);
	canvas.width = baseLength;
	canvas.height = canvas.width;
	gameBoard.redraw();
}

resizeCanvas();

function onBoardDataReceived(game) {
	var pieces = [];
	var validMoves = [];
	var num = new Uint16Array(game, 0);
	
	window.location.hash='#'+encodeBase64GameState(num);
	
	var black = 0, white = 0;
	for(var i = 0; i < num.length-1; ++i) {
		var n = num[i];

		var binaryString = (("0000000000000000" + n.toString(2)).slice(-16));
		console.log(i + "  " + binaryString);

		for (var j = 0, len = binaryString.length; j < len; j=j+2) {
			var cell = binaryString[j] + binaryString[j+1];

			if(cell === '01') {
				pieces.push("B");
				black++;
			} else if(cell === '10') {
				pieces.push("W");
				white++;
			} else if(cell === '11') {
				pieces.push("N");
				validMoves.push(i*8+j/2);
			} else {
				pieces.push("N");
			}
		}
	}
	var who = num[num.length-1];

	console.log("WHO(0:B,1:W,2:GAMEOVER):" + who);
	console.log("PIECES:" + pieces);
	console.log("VALID MOVES:" + validMoves);
	console.log("Black:" + black);
	console.log("White:" + white);

	if(who == 2) {
		var winner = "Both";
		if(black < white) {
			winner = "White";
		} else if (white < black) {
			winner = "Black";
		}
		alert("Game Over! Winner is " + winner);
	} else{
		if(who == myTurn) {
			//BLACK TURN (CURRENT MY TURN ONLY)
			console.log("Black Turn");
			gameBoard.lockPut(false);
		} else{ 
			//WHITE TURN (CURRENT PC TURN ONLY)
			console.log("White Turn");
			validMoves = []; // To clear valid move when ai turn
			gameBoard.lockPut(true);
			var xhrBestMove = new XMLHttpRequest();
			xhrBestMove.open('POST', '/game/bestmove', true);
			xhrBestMove.responseType = 'arraybuffer';
			
			xhrBestMove.onload = function(e) {
				if (this.status == 200) {
					var resBuffer = xhrBestMove.response;
					var resData = new Uint16Array(resBuffer, 0);
					var bestMove = resData[0];
					console.log("BestMove:" + bestMove);
					setTimeout(function() { gameBoard.put(bestMove); }, 1000);
				}
			};
			
			var bestMoveSendData = new Uint16Array(num.length+1);
			bestMoveSendData.set(num);
			bestMoveSendData[num.length] = 3;
			xhrBestMove.send(bestMoveSendData);
		}
	}
	
	gameBoard.drawPieces(who, pieces, validMoves);
}

var xhrNewGame = new XMLHttpRequest();
xhrNewGame.open('POST', '/game/new', true);
xhrNewGame.responseType = 'arraybuffer';

xhrNewGame.onload = function(e) {
	if (this.status == 200) {
		onBoardDataReceived(xhrNewGame.response);
	}
};

if(window.location.hash.length > 1) {
	var encodedHash = window.location.hash.replace("#","");
	var gameState = decodeBase64GameState(encodedHash);
	onBoardDataReceived(gameState);
} else {
	xhrNewGame.send();
}

function decodeBase64GameState(b64EncodedGameState) {
	var u8 = new Uint8Array(atob(b64EncodedGameState).split("").map(function(c) {
	    return c.charCodeAt(0); 
    }));
	return new Uint16Array(u8.buffer);
}

function encodeBase64GameState(u16State) {
	var u8 = new Uint8Array(u16State.buffer);
	return btoa(String.fromCharCode.apply(null, u8));
}