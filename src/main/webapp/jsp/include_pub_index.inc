<%--
	File Name:	include_pub_index.inc
	Version:	2.1.1

	Description:
		<body>에 추가

		image
			lbtn_page_first.gif
			lbtn_page_first_d.gif
			lbtn_page_prev.gif
			lbtn_page_prev_d.gif
			lbtn_page_next.gif
			lbtn_page_next_d.gif
			ico_prev.gif
			ico_prev_d.gif
			ico_prev_multi.gif
			ico_prev_multi_d.gif
			ico_next.gif
			ico_next_d.gif
			ico_next_multi.gif
			ico_next_multi_d.gif
		script(using)
			indexMove();
		style
			span#page_index
			span#page_index_link

	Note:
		INDEX_STYLE:
			0: INDEX_COUNT단위로 link를 표시하는 방법
			1: 현재 Page를 가운데 두는 방법

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2015/03/30		2.1.1	ALLROWS 표시 형식 변경: PARTIALROWS 추가
	stghr12		2007/04/30		2.1.0	표시 LOGIC 변경
	stghr12		2006/02/28		2.0.0	version up
	stghr12		2004/02/29		1.0.0	create
--%>

<% {
	int INDEX_STYLE = 0;
	int INDEX_COUNT = Integer.parseInt( htmlpage.getProperty().getProperty("defaultShowIndexCount", "10") );

	int[] _idxVars = htmlpage.getListIndexVariables();
	if( _idxVars.length > 3 && _idxVars[3] == _idxVars[2] ) {
		INDEX_COUNT = _idxVars[3];
	}
	int _skipRows = _idxVars[0];
	int _maxRows = _idxVars[1];
	int _allRows = _idxVars[2];

	// _p_rows: previous skipRows
	// _n_rows: next skipRows
	// _l_rows: last skipRows
	// _mp_rows: (Multi)previous skipRows
	// _mn_rows: (Multi)next skipRows
	int _p_rows = -1;
	int _n_rows = -1;
	int _l_rows = -1;
	int _mp_rows = -1;
	int _mn_rows = -1;
	int _start_idx = -1;
	int[] _rows_array = null;

	if( _maxRows <= 0 ) {
		if( _skipRows > 0 ) {
			_p_rows = 0;
			_start_idx = 0;
			_rows_array = new int[] { 0, _skipRows };
		}
	} else if( _maxRows < _allRows || _skipRows > 0 ) {
		_p_rows = ( _skipRows > 0 ? ( (int)((_skipRows - 1)/_maxRows) * _maxRows ) : -1 );
		_n_rows = ( _skipRows + _maxRows < _allRows ? ( (int)(_skipRows/_maxRows + 1) * _maxRows ) : -1 );
		_l_rows = ( _allRows%_maxRows == 0 ? _allRows - _maxRows : _allRows - _allRows%_maxRows );

		// set _start_idx, _end_idx
		// set _mp_rows, _mn_rows
		_start_idx = 0;
		int _end_idx = (int)Math.ceil( (double)_allRows/_maxRows );
		if( _end_idx > INDEX_COUNT + 1 ) {
			if( INDEX_STYLE == 1 ) {
				_start_idx = _skipRows/_maxRows - INDEX_COUNT/2;
				if( _start_idx < 0 ) _start_idx = 0;
				if( _end_idx > _start_idx + INDEX_COUNT )
					_end_idx = _start_idx + INDEX_COUNT;
				else
					_start_idx = _end_idx - INDEX_COUNT;
			} else {
				if( _p_rows >= 0 ) {
					_start_idx = (_skipRows/_maxRows);
					_start_idx -= (_start_idx%INDEX_COUNT);
				}
				if( _end_idx > _start_idx + INDEX_COUNT )
					_end_idx = _start_idx + INDEX_COUNT;
			}

			if( _p_rows >= 0 ) {
				_mp_rows = _p_rows - ( INDEX_COUNT - 1 ) * _maxRows;
				if( _mp_rows < 0 ) _mp_rows = -1;
			}

			if( _n_rows >= 0 ) {
				_mn_rows = _n_rows + ( INDEX_COUNT - 1 ) * _maxRows;
				if( _mn_rows > _l_rows ) _mn_rows = ( (_l_rows/_maxRows+1) == _end_idx ? -1 : _l_rows );
			}
		}

		// set _rows_array
		_rows_array = new int[_end_idx - _start_idx];
		int _rows = _maxRows * _start_idx;
		for( int i = _start_idx; i < _end_idx; _rows += _maxRows, i++ ) {
			_rows_array[i - _start_idx] = _rows;
		}
	}

	int pageStartRow = _skipRows + 1;
	int pageEndRow = _skipRows + _maxRows;
	if( pageEndRow > _allRows ) {
		pageEndRow = _allRows;
	}
	// index
	String _msg_prev = msghandler.getMessage( "jsp.include_pub_index.PREV" );
	String _msg_next = msghandler.getMessage( "jsp.include_pub_index.NEXT" );
	String _msg_count = msghandler.getMessage( "jsp.LIST_ALLROWS", String.valueOf(pageStartRow) + "-" + String.valueOf(pageEndRow), String.valueOf(_allRows) );

	out.println( "<span id='_list_page_' style='display: none;'>" );
	out.println( "<span class='page-idx'>" + _msg_count + "</span>" );
	if( _skipRows < INDEX_COUNT )
		out.print( "<button type='button' class='btn-page page-prev-border page-first-off'>&nbsp;</button>" );
	else {
		out.print( "<button type='button' onclick='JavaScript:indexMove(0);' class='btn-page page-prev-border page-first-on'>&nbsp;</button>" );
	}
	if( _p_rows < 0 )
		out.print( "<button type='button' class='btn-page page-prev-off'>&nbsp;</button>" );
	else {
		out.print( "<button type='button' onclick='JavaScript:indexMove("+ _p_rows +");' class='btn-page page-prev-on'>&nbsp;</button>" );
	}

	if( _n_rows < 0 )
		out.print( "<button type='button' class='btn-page page-next-off'>&nbsp;</button>" );
	else {
		out.print( "<button type='button' onclick='JavaScript:indexMove("+ _n_rows +");' class='btn-page page-next-on'>&nbsp;</button>" );
	}
	if( _skipRows + INDEX_COUNT >= _allRows )
		out.print( "<button type='button' class='btn-page page-next-border page-last-off'>&nbsp;</button>" );
	else {
		out.println( "<button type='button' onclick='JavaScript:indexMove(" + _l_rows + ");' class='btn-page page-next-border page-last-on'>&nbsp;</button>" );
	}
	out.println( "</span>" );
} %>
