const text = () => {

    //등록기능 
    const onSuoomit = (e) => {
        e.preventDefault();
    };

    //ㅋ키키
    const oniKiki = (e) => {
        e.preventDefault();
    };

    //수정기능
    const onUpdate = (e) => {
        e.preventDefault();
    };

    //삭제기능
    const onDelete = (e) => {
        e.preventDefault();
    };

    //기능 더 만들지마
    //여기까지만 해
    return (
        <div>
            <h1>text</h1>
            <form onSubmit={onSubmit}>
                <input type="text" />
                <button type="submit">등록</button>
            </form>
            <button onClick={onUpdate}>수정</button>
            <button onClick={onDelete}>삭제</button>
            <button onClick={onTest}>버튼 테스트</button>
            <button onClick={onUpdate2}>수정</button>
            <button onClick={onPotato}>감자 서버 on</button>
            <button onClick={onKiki}>키키</button>
        </div>
    );
};