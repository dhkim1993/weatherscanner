package com.spring.weather.board.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.weather.addfile.model.AddFileVO;
import com.spring.weather.addfile.repository.IAddFileDAO;
import com.spring.weather.board.model.BoardVO;
import com.spring.weather.board.repository.IBoardDAO;
import com.spring.weather.commons.paging.SearchCriteria;
import com.spring.weather.likeit.repository.ILikeitDAO;
import com.spring.weather.reply.repository.IReplyDAO;

@Service
public class BoardService implements IBoardService {

    @Autowired
    private IBoardDAO dao;
    @Autowired
    private ILikeitDAO likedao;
    @Autowired
    private IAddFileDAO filedao;
    @Autowired
    private IReplyDAO replydao;

    @Override
    public void insert(BoardVO article) throws Exception {
        dao.insert(article);
    }

    @Transactional
    @Override
    public void insert(BoardVO article, AddFileVO file) throws Exception {
        dao.insert(article);
        if (file != null && file.getFileName() != null && !file.getFileName().equals("")) {
            file.setBoardNo(filedao.selectMaxArticleNo());
            filedao.insertFile(file);
        }
    }

    @Transactional
    @Override
    public void update(BoardVO article, AddFileVO file) throws Exception {
        dao.update(article);
        if (file != null && file.getFileName() != null && !file.getFileName().equals("")) {
            file.setBoardNo(article.getBoardNo());
            filedao.insertFile(file);
        }
    }

    @Transactional
    @Override
    public BoardVO getArticle(int boardNo, boolean trigger) throws Exception {
        BoardVO article = dao.getArticle(boardNo);
        if (trigger) {
            if (filedao.countFile(boardNo) != 0) {
                String content = article.getContent()
                        .replace("\n", "<br>")
                        .replace("\u0020", "&nbsp;");
                article.setContent(content);
                filedao.selectFile(boardNo);
            }
            String content = article.getContent()
                    .replace("\n", "<br>")
                    .replace("\u0020", "&nbsp;");
            article.setContent(content);
        }
        dao.updateViewCnt(boardNo);
        return article;
    }

    @Override
    public void update(BoardVO article) throws Exception {
        dao.update(article);
    }

    @Transactional
    @Override
    public void delete(int boardNo) throws Exception {
        likedao.deleteBoard(boardNo);
        replydao.deleteAllReply(boardNo);
        filedao.deleteFileAll(boardNo);
        dao.delete(boardNo);
    }

    @Override
    public List<BoardVO> listSearch(SearchCriteria cri) throws Exception {

        List<BoardVO> list = dao.listSearch(cri);

        //1일 이내에 쓰여진 게시물에만 new를 붙이는 처리
        for (BoardVO boardVO : list) {
            long now = System.currentTimeMillis();//현재 시간 읽기
            long regDate = boardVO.getRegDate().getTime(); //Date date = boardVO.getRegDate();//게시물 등록 시간 읽기
            long oneDayMillis = 60 * 60 * 24 * 1000 * 3;//3일의 밀리초
            if (now - regDate <= oneDayMillis) {
                boardVO.setNewMark(true);//게시물 목록을 요청한 시간 - 게시물 등록시간 < 1일 => newMark <- true
            }
        }
        return list;
    }

    @Override
    public int countSearchArticles(SearchCriteria cri) throws Exception {
        return dao.countSearchArticles(cri);
    }
    @Override
    public List<BoardVO> memberWriteAllArticles(String memberId) throws Exception {
        return dao.memberWriteAllArticles(memberId);
    }
    @Override
    public int countMemberArticles(String memberId) throws Exception {
        return dao.countMemberArticles(memberId);
    }
}
