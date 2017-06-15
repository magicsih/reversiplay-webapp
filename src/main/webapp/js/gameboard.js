var GameBoard = function(canvasId, size, listener) {
	const NONE = "N";
	const WHITE = "W";
	const BLACK = "B";
	const WHITE_COLOR = "#FFF";
	const BLACK_COLOR = "#000";
	const VALID_COLOR = "rgba(255, 255, 0, 0.7)";
	const VALID_COLOR_BEST = "Red";
	const PIECE_MARGIN = 0.8;

	this.canvas = document.getElementById(canvasId);
	this.ctx = this.canvas.getContext("2d");
	this.listener = listener;

	var currentBoard = [];
	var currentTurn = 0; //0:Black 1:White
	var validMoves = [];
	var validBestMove = -1;
	var size = size;
	var boardCellColor = "#333";
	var boardCellColorEven = "#999";
	var self = this;
	var lockForMouseDown = false;

	console.log(this.canvas.width + "x" + this.canvas.height);

	this.canvas.addEventListener('mousedown', function(evt) {
		if(lockForMouseDown) return;
		var rect = evt.currentTarget.getBoundingClientRect();
		var x = evt.clientX - rect.left;
		var y = evt.clientY - rect.top;

		console.log(evt.clientX + "-" + rect.left +"=" + x);
		console.log(evt.clientY + "-" + rect.top +"=" + y);
		onPiecePut.call(self,x,y);
	});

	function onPiecePut(x,y) {
		var row = size - parseInt((this.canvas.height - y) / (this.canvas.height / size)) - 1;
		var col = size - parseInt((this.canvas.width - x) / (this.canvas.width / size)) - 1;

		var isValid = false;
		var index = row * size + col;
		for(var i in validMoves) {
			if(validMoves[i] == index) {
				isValid = true;
				break;
			}
		}

		if(isValid == false) return;

		this.listener.onPiecePut(currentTurn, currentBoard, row,col);
	}

	function drawBoard() {
		var even = false;

		for(var r = 0; r < size;++r) {
			var rowPosition = r * (this.canvas.width / size);
			for(var c=0;c < size;++c) {
				var colPosition = c * (this.canvas.height / size);
				if(even) {
					this.ctx.fillStyle = boardCellColorEven;
				} else{
					this.ctx.fillStyle = boardCellColor;
				}
				even = !even;
				this.ctx.fillRect(colPosition,rowPosition, this.canvas.width / size, this.canvas.height / size);
			}
			even = !even;
		}
	};

	this.setBoardCellColor = function(cell, even) {
		boardCellColor = cell;
		boardCellColorEven = even; 
	};

	function drawPiece(r,c,piece) {
		var rowPosition = r * (this.canvas.width / size);
		var colPosition = c * (this.canvas.height / size);
		this.ctx.beginPath();
		if(piece === WHITE) {
			this.ctx.fillStyle = WHITE_COLOR;
		} else if (piece === BLACK) {
			this.ctx.fillStyle = BLACK_COLOR;
		}
		this.ctx.arc(colPosition + (this.canvas.height / size / 2), rowPosition + (this.canvas.width /  size / 2), this.canvas.width/ size/2 - (this.canvas.width /size /2 * (1-PIECE_MARGIN)), 0, 2*Math.PI);
		this.ctx.fill();
	};


	function reversePiece(r,c,from,to) {
		var rowPosition = r * (this.canvas.width / size);
		var colPosition = c * (this.canvas.height / size);
		this.ctx.beginPath();
		if(to === WHITE) {
			this.ctx.fillStyle = WHITE_COLOR;
			this.ctx.strokeStyle = BLACK_COLOR;
		} else if (to === BLACK) {
			this.ctx.fillStyle = BLACK_COLOR;
			this.ctx.strokeStyle = WHITE_COLOR;
		}
		this.ctx.arc(colPosition + (this.canvas.height / size / 2), rowPosition + (this.canvas.width /  size / 2), this.canvas.width/ size/2 - (this.canvas.width /size /2 * (1-PIECE_MARGIN)), 0, 2*Math.PI);
		this.ctx.fill();
		this.ctx.lineWidth = 2;
		this.ctx.stroke();
	};
	
	this.put = function(position) {
		var row = parseInt(position / size);
		var col = parseInt(position - (row * size));
		console.log("AI Move:" + row +"," +col);
		this.listener.onPiecePut(currentTurn, currentBoard, row,col);
	};
	
	this.redraw = function() {
		this.drawPieces(currentTurn, currentBoard, validMoves, validBestMove);
	};
	this.drawPieces = function(turn, pieces, vMoves) {
		drawBoard.call(self);
		validMoves = vMoves;

		var lastBoardState = currentBoard.slice(0); // clone the current board state
		currentTurn = turn;
		currentBoard = pieces;

		for(var r = 0 ; r < size; r++) {
			for(var c = 0 ; c < size; c++) {
				var index = r * size + c;
				var piece = pieces[index];

				if(lastBoardState.length == currentBoard.length) {
					var previousPiece = lastBoardState[index];
					//Catch pieces only needed to reverse
					if(piece !== NONE && previousPiece !== NONE && piece !== previousPiece) {
						if(previousPiece === WHITE) {
							//Transitioning White to Black
							reversePiece.call(self, r, c, WHITE, BLACK);
						} else if(previousPiece === BLACK) {
							reversePiece.call(self, r, c, BLACK, WHITE);
						}
					} else{
						if(piece === WHITE) {
							drawPiece.call(self,r, c, WHITE);
						} else if (piece === BLACK) {
							drawPiece.call(self,r, c, BLACK);
						}
					}
				} else{
					if(piece === WHITE) {
						drawPiece.call(self, r,c,WHITE);
					} else if (piece === BLACK) {
						drawPiece.call(self, r,c,BLACK);
					}
				}
			}
		}

		for(var i in validMoves) {
			var row = parseInt(validMoves[i] / size);
			var col = parseInt(validMoves[i] - (row * size));

			var rowPosition = row * (this.canvas.width / size);
			var colPosition = col * (this.canvas.height / size);

			this.ctx.beginPath();
			this.ctx.fillStyle = VALID_COLOR;
			this.ctx.arc(colPosition + (this.canvas.height / size / 2), rowPosition + (this.canvas.width /  size / 2), this.canvas.width/ size/2 - (this.canvas.width / size / 3), 0, 2*Math.PI);
			this.ctx.fill();
		}

	};

	this.saveAsImage = function() {
		var w=window.open('about:blank','image from canvas');
		w.document.write("<img src='"+this.canvas.toDataURL("image/png")+"' alt='from canvas'/>");
	};
	
	this.lockPut = function(lock) {
		lockForMouseDown = lock;
	}
};