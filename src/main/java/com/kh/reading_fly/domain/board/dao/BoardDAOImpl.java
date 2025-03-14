package com.kh.reading_fly.domain.board.dao;

import com.kh.reading_fly.domain.board.dto.BoardDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class BoardDAOImpl implements BoardDAO{

  private final JdbcTemplate jdbcTemplate;
//  private JdbcTemplate jdbcTemplate;
//  public BoardDAOImpl(JdbcTemplate jdbcTemplate){
//    this.jdbcTemplate = jdbcTemplate;
//  }@RequiredArgsConstructor 로 생성

  /**
   * 전체조회
   * @return 게시글 List
   */
  @Override
  public List<BoardDTO> selectAll() {
    //sql 작성
    StringBuffer sql = new StringBuffer();
    sql.append(" select row_number() over (order by bcdate) as num, bnum, btitle, bcdate, budate, bhit, nickname ");
    sql.append(" from board, member ");
    sql.append(" where board.bid = member.id ");
    sql.append(" order by bcdate desc ");

    //sql 실행
    List<BoardDTO> list = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(BoardDTO.class));

    return list;
  }

  /**
   * 상세조회
   * @param bnum
   * @return 게시글 BoardDTO
   */
  @Override
  public BoardDTO selectOne(Long bnum) {
    //sql 작성
    StringBuffer sql = new StringBuffer();
    sql.append(" select bnum, btitle, bcdate, budate, bcontent, bhit, bid, nickname ");
    sql.append(" from board, member ");
    sql.append(" where board.bid = member.id and bnum = ? ");

    //sql 실행
    List<BoardDTO> query = jdbcTemplate.query(
            sql.toString(),//1. sql 문
            new BeanPropertyRowMapper<>(BoardDTO.class),//2. 자동맵핑(setter 메소드를 통해 board 객체에 삽입)
            bnum//3. 조회할 게시글 번호
    );
    
    return (query.size() == 1)? query.get(0) : null;//찾은 게시글(인덱스0)반환
  }

  /**
   * 등록
   * @param board
   * @return
   */
  @Override
  public BoardDTO create(BoardDTO board) {
    //sql 작성
    StringBuffer sql = new StringBuffer();
    sql.append(" insert into board (bnum, btitle, bcontent, bid) ");
    sql.append(" values (board_bnum_seq.nextval, ?, ?, ?) ");

    //sql 실행
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(new PreparedStatementCreator() {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement pstmt = con.prepareStatement(
                sql.toString(),               //1. sql 문 db 에서 실행
                new String[]{"bnum"}          //2. insert 후 insert 레코드 중 반환할 컬럼명(시퀀스) KeyHolder 에 저장됨
        );

        pstmt.setString(1, board.getBtitle());
        pstmt.setString(2, board.getBcontent());
        pstmt.setString(3, board.getBid());

        return pstmt;
      }
    }, keyHolder);//notice_id 를 keyHolder 에 담아 반환(Map<>인가?)

    Long bnum = Long.valueOf(keyHolder.getKeys().get("bnum").toString());
    return selectOne(bnum);
  }

  /**
   * 수정
   * @param board
   * @return
   */
  @Override
  public BoardDTO update(BoardDTO board) {

    StringBuffer sql = new StringBuffer();
    sql.append(" update board ");
    sql.append(" set btitle = ? , ");
    sql.append("     bcontent = ? , ");
    sql.append("     budate = systimestamp ");
    sql.append(" where bnum = ? and bid = ? ");

    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(new PreparedStatementCreator() {
      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement pstmt = con.prepareStatement(
                sql.toString(),
                new String[]{"bnum"}
        );

        pstmt.setString(1, board.getBtitle());
        pstmt.setString(2, board.getBcontent());
        pstmt.setString(3, String.valueOf(board.getBnum()));
        pstmt.setString(4, board.getBid());

        return pstmt;
      }
    }, keyHolder);

    Long bnum = Long.valueOf(keyHolder.getKeys().get("bnum").toString());
    return selectOne(bnum);
  }

  /**
   * 삭제
   * @param bnum
   * @return
   */
  @Override
  public int delete(Long bnum, String bid) {

    StringBuffer sql = new StringBuffer();
    sql.append(" delete from board ");
    sql.append(" where bnum = ? and bid = ? ");

    int count = jdbcTemplate.update(sql.toString(), bnum, bid);//성공시 1 실패시 0(update 의 반환 타입은 int)

    return count;
  }

  /**
   * 조회수 증가
   * @param bnum
   * @return
   */
  @Override
  public int updateHit(Long bnum) {

    StringBuffer sql = new StringBuffer();
    sql.append(" update board ");
    sql.append(" set bhit = bhit + 1 ");
    sql.append(" where bnum = ? ");

    int cnt = jdbcTemplate.update(sql.toString(), bnum);//성공시 1 실패시 0(update 의 반환 타입은 int)

    return cnt;
  }
}
