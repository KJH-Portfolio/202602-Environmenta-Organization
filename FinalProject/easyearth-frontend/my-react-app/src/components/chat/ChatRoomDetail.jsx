import React, { useCallback, useEffect, useLayoutEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useChat } from '../../context/ChatContext';
import { useNotification } from '../../context/NotificationContext';
import { 
    clearNotice, 
    getChatRoomDetail, 
    getChatRoomUsers, 
    getMessages, 
    kickMember, 
    leaveChatRoom, 
    markAsRead, 
    searchMessages, 
    setNotice, 
    updateRole 
} from '../../apis/chatApi'; 
import { getFullUrl } from '../../utils/chatImageUtil';
import { extractOriginalFileName } from './chatFileUtil';
import CustomModal from '../common/CustomModal';
import UserDatailModal from '../common/UserDatailModal';
import FileUploadButton from './FileUploadButton';
import MessageBubble from './MessageBubble';
import MemberManagementModal from './MemberManagementModal';
import styles from './ChatRoomDetail.module.css';

const ChatRoomDetail = ({ roomId }) => {
    const { client, connected, loadChatRooms, lastReadEvent } = useChat();
    const { user } = useAuth();
    const { markNotificationsAsReadForRoom } = useNotification();
    const navigate = useNavigate();

    const [messages, setMessages] = useState([]);
    const [isReady, setIsReady] = useState(false);
    const [showLastReadBtn, setShowLastReadBtn] = useState(false); // 최근 읽은 메시지 버튼
    const isFirstLoad = useRef(true);
    const [input, setInput] = useState('');
    const [hasMore, setHasMoreState] = useState(true);
    const hasMoreRef = useRef(true);
    const messagesEndRef = useRef(null);
    const observerTarget = useRef(null);
    const prevScrollHeight = useRef(0);
    const chatInputRef = useRef(null);

    // 방 변경 시 입력창 자동 포커스
    useEffect(() => {
        if (chatInputRef.current) {
            setTimeout(() => {
                chatInputRef.current.focus();
            }, 100);
        }
    }, [roomId]);

    const setHasMore = (val) => {
        hasMoreRef.current = val;
        setHasMoreState(val);
    };

    const [showMemberModal, setShowMemberModal] = useState(false);
    const [roomMembers, setRoomMembers] = useState([]);
    const [roomInfo, setRoomInfo] = useState({ title: '', type: 'SINGLE', members: [], creatorId: null, noticeContent: null, noticeMessageId: null, roomImage: null });

    const [replyTo, setReplyTo] = useState(null);
    const [incomingNotifications, setIncomingNotifications] = useState([]);
    const [outgoingNotifications, setOutgoingNotifications] = useState([]);

    // 알림 토스트 추가 (최대 3개, 5초 후 자동 삭제)
    const addNotification = (setter, message) => {
        const id = Date.now() + Math.random();
        const newNoti = { ...message, _id: id, closing: false };

        setter(prev => {
            const next = [...prev, newNoti];
            if (next.length > 3) next.shift(); // Keep max 3
            return next;
        });

        // Trigger Fade Out after 4.7s
        setTimeout(() => {
            setter(prev => prev.map(n => n._id === id ? { ...n, closing: true } : n));
        }, 4700);

        // Auto remove after 5s
        setTimeout(() => {
            setter(prev => prev.filter(n => n._id !== id));
        }, 5000);
    };

    const [modalConfig, setModalConfig] = useState({
        isOpen: false, title: "", message: "", type: "alert", onConfirm: null, onCancel: null
    });

    // 검색 관련 State
    const [showSearch, setShowSearch] = useState(false);
    const [searchKeyword, setSearchKeyword] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [currentSearchIndex, setCurrentSearchIndex] = useState(-1);
    const [highlightedMessageId, setHighlightedMessageId] = useState(null);
    const [searchOffset, setSearchOffset] = useState(0);
    const [hasMoreSearchResults, setHasMoreSearchResults] = useState(false);
    
    // 마지막 읽은 메시지 마커 ID
    const [lastReadMarkerId, setLastReadMarkerId] = useState(null);
    const lastReadMarkerRef = useRef(null);

    const [showProfileModal, setShowProfileModal] = useState(false);

    // 헤더 메뉴 State
    const [showMenu, setShowMenu] = useState(false);
    const menuRef = useRef(null);

    // Close menu when clicking outside
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (menuRef.current && !menuRef.current.contains(event.target)) {
                setShowMenu(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const closeModal = useCallback(() => {
        setModalConfig(prev => ({ ...prev, isOpen: false }));
        // 모달 닫힘 시 입력창 포커스 복귀
        if (chatInputRef.current) {
            chatInputRef.current.focus();
        }
    }, []);

    const showAlert = useCallback((message, title = "알림") => {
        setModalConfig({ isOpen: true, title, message, type: "alert", onConfirm: closeModal, onCancel: closeModal });
    }, [closeModal]);

    const showConfirm = useCallback((message, onConfirm, title = "확인") => {
        setModalConfig({ isOpen: true, title, message, type: "confirm", onConfirm: () => { onConfirm(); closeModal(); }, onCancel: closeModal });
    }, [closeModal]);

    // 채팅방 상세 정보 조회 (데이터 반환만 담당, 마커 설정은 호출부에서 처리)
    const fetchRoomInfo = useCallback(async () => {
        if (isLeaving.current) return null;
        try {
            const data = await getChatRoomDetail(roomId, user?.memberId);
            setRoomInfo(data);
            return data;
        } catch (error) {
            const status = error.response?.status;
            if (status === 404) {
                // [방어] 채팅방 없음 - 모달 확인 후 목록으로 이동
                setModalConfig({
                    isOpen: true,
                    title: "채팅방 없음",
                    message: "해당 채팅방을 찾을 수 없거나 삭제되었습니다.",
                    type: "alert",
                    onConfirm: () => navigate('/chat'),
                    onCancel: null
                });
            } else if (status === 403) {
                // 권한이 없는 방(그룹/개인)에 URL로 강제 접속 시도한 경우 차단 알림
                console.warn("403 Forbidden: 접근 권한이 없거나 세션이 만료되었습니다.");
                setModalConfig({
                    isOpen: true,
                    title: "접근 권한 없음",
                    message: "해당 채팅방에 참여하고 있지 않거나 접근 권한이 없습니다.",
                    type: "alert",
                    onConfirm: () => navigate('/chat'),
                    onCancel: null
                });
                return null;
            } else {
                console.error("채팅방 정보 로드 실패", error);
                setModalConfig({
                    isOpen: true,
                    title: "오류",
                    message: "채팅방 정보를 불러올 수 없습니다. 잠시 후 다시 시도해주세요.",
                    type: "alert",
                    onConfirm: null,
                    onCancel: null
                });
            }
            return null;
        }
    }, [roomId, user?.memberId, navigate]);

    // 메시지 목록 조회 (커서 기반 페이징)
    const fetchMessages = useCallback(async (cursorId) => {
        if (isLeaving.current) return [];
        try {
            if (!hasMoreRef.current && cursorId !== 0) return;
            if (!user) return;

            const data = await getMessages(roomId, cursorId, user.memberId, 30);
            
            if (data.length === 0) {
                setHasMore(false);
                return;
            }

            if (cursorId === 0) {
                // ✨ [Fix] localId 할당 logic
                const messagesWithLocalId = data.map(msg => ({ ...msg, localId: msg.messageId }));
                setMessages(messagesWithLocalId);
            } else {
                // 이전 스크롤 높이 저장 (위치 보정용)
                if (messagesContainerRef.current) {
                    prevScrollHeight.current = messagesContainerRef.current.scrollHeight;
                }
                const messagesWithLocalId = data.map(msg => ({ ...msg, localId: msg.messageId }));
                setMessages(prev => [...messagesWithLocalId, ...prev]);
            }
            
             if (data.length < 30) setHasMore(false);
             
             return data; // [Fix] 데이터 반환

        } catch (error) {
            console.error("메시지 로드 실패", error);
            showAlert("메시지를 불러올 수 없습니다. 페이지를 새로고침해주세요.");
            return []; // 에러 시 빈 배열 반환
        }
    }, [roomId, user?.memberId]); // roomInfo 의존성 제거 (unreadCount 로직 삭제됨)
    
    // 방 입장 초기화 (메시지/정보 로드)
    useEffect(() => {
        if (!connected || !roomId || !user) return;

        isFirstLoad.current = true;
        setIsReady(false); // [Fix] 방 전환 시 즉시 숨김 (이전 방 잔상 방지)

        const initializeRoom = async () => {
            if (isLeaving.current) return;
            try {
                setMessages([]);
                setHasMore(true);
                setReplyTo(null);
                setLastReadMarkerId(null);
                isInitialScrollComplete.current = false;

                // 1. 방 정보 로드
                const roomData = await fetchRoomInfo();

                if (!roomData) {
                    setIsReady(true);
                    return;
                }

                // 2. [근본 해결] 메시지 로드 전에 먼저 읽음 처리를 수행하여
                // 서버가 나를 '읽은 상태'로 반영한 뒤 메시지 목록을 가져오게 함.
                await markAsRead(roomId, user.memberId, null);
                await loadChatRooms();

                // 3. 메시지 로드 (이제 나를 제외한 정확한 unreadCount를 포함하게 됨)
                const messagesData = await fetchMessages(0);

                // 4. 스크롤 위치 보정 및 화면 표시
                const lastReadId = roomData.myLastReadMessageId;
                if (lastReadId && messagesData && messagesData.length > 0) {
                    const latestMsgId = messagesData[messagesData.length - 1].messageId;
                    
                    if (lastReadId < latestMsgId) {
                        setLastReadMarkerId(lastReadId);
                        setShowLastReadBtn(true);
                    } else {
                        setLastReadMarkerId(null);
                        setShowLastReadBtn(false);
                    }
                } else {
                    setShowLastReadBtn(false);
                }

                setTimeout(() => {
                    if (messagesEndRef.current) {
                        messagesEndRef.current.scrollIntoView({ behavior: 'auto' });
                    }
                    isInitialScrollComplete.current = true;
                    setIsReady(true);
                }, 80);

                isFirstLoad.current = false;

            } catch (error) {
                console.error("채팅방 초기화 실패", error);
                setIsReady(true);
            }
        };

        initializeRoom();
        markNotificationsAsReadForRoom(roomId);

        return () => {};
    }, [roomId, connected, user?.memberId]);
    
    // [단순화] 마커 useEffect는 초기 진입 시 제거하고 initializeRoom에서 직접 처리
    // 무한스크롤로 과거 메시지 추가 로드 후에도 위치가 유지되도록 안전장치만 유지
    useEffect(() => {
        // 초기 진입 시에는 initializeRoom의 setTimeout이 처리하므로 여기서는 무시
        // isInitialScrollComplete가 true인 경우(무한스크롤 후)에만 동작하지 않도록 처리
    }, [lastReadMarkerId]);

    // 개인 채널로 수신된 READ_UPDATE 감지 (발신자가 방 밖에 있을 때도 메시지 옆 1 갱신)
    useEffect(() => {
        if (!lastReadEvent) return;
        if (String(lastReadEvent.chatRoomId) !== String(roomId)) return;

        const { unreadCountMap } = lastReadEvent;
        if (!unreadCountMap) return;

        // unreadCountMap에 포함된 메시지만 정확히 업데이트 (나머지는 건드리지 않음)
        setMessages(prev => prev.map(msg => {
            const key = String(msg.messageId);
            if (Object.prototype.hasOwnProperty.call(unreadCountMap, key)) {
                return { ...msg, unreadCount: unreadCountMap[key] };
            }
            return msg;
        }));
    }, [lastReadEvent, roomId]);




    // WebSocket 실시간 구독 (채팅방, 리액션, 읽음, 개인 채널)
    useEffect(() => {
        if (!client || !connected || !roomId) return;

        const roomSubscription = client.subscribe(`/topic/chat/room/${roomId}`, (message) => {
            const receivedMsg = JSON.parse(message.body);

            if (receivedMsg.type === 'ROOM_UPDATE') {
                setRoomInfo(prev => ({
                    ...prev,
                    title: receivedMsg.title !== undefined ? receivedMsg.title : prev.title,
                    roomImage: receivedMsg.roomImage !== undefined ? receivedMsg.roomImage : prev.roomImage
                }));
                loadChatRooms(); // 목록 사이드바 정보 동시 갱신
                return;
            }

            if (receivedMsg.type === 'NOTICE_UPDATED') {
                console.log("공지 업데이트 수신:", receivedMsg);
                setRoomInfo(prev => ({ 
                    ...prev, 
                    noticeContent: receivedMsg.noticeContent, 
                    noticeMessageId: receivedMsg.noticeMessageId, 
                    noticeSenderName: receivedMsg.senderName 
                }));
                return;
            }

            if (receivedMsg.type === 'NOTICE_CLEARED') {
                setRoomInfo(prev => ({ 
                    ...prev, 
                    noticeContent: null, 
                    noticeMessageId: null, 
                    noticeSenderName: null 
                }));
                return;
            }

            if (receivedMsg.type === 'MEMBER_UPDATE') {
                fetchRoomInfo();
                if (showMemberModal) { 
                    getChatRoomUsers(roomId).then(data => setRoomMembers(data));
                }
                return;
            }

            // Message handling
            setMessages(prev => {
                const receivedId = String(receivedMsg.messageId || receivedMsg.id);
                // 중복 체크 및 업데이트 로직
                const existingIndex = prev.findIndex(msg => String(msg.messageId || msg.id) === receivedId);

                // 내 메시지 매칭 (낙관적 업데이트 대체)
                let optimisticIndex = -1;
                if (receivedMsg.senderId === user.memberId) {
                    optimisticIndex = prev.findIndex(msg =>
                        msg.isOptimistic &&
                        msg.content === receivedMsg.content &&
                        msg.messageType === receivedMsg.messageType
                    );
                }

                let updatedMessages = [...prev];

                if (existingIndex !== -1) {
                    // 기존 메시지 업데이트 (localId 유지)
                    const existingMsg = updatedMessages[existingIndex];
                    updatedMessages[existingIndex] = { ...existingMsg, ...receivedMsg, localId: existingMsg.localId };
                } else if (optimisticIndex !== -1) {
                    // 낙관적 메시지를 실제 메시지로 교체
                    const optimisticMsg = updatedMessages[optimisticIndex];
                    updatedMessages[optimisticIndex] = { ...receivedMsg, localId: optimisticMsg.localId };
                } else {
                    // 새 메시지 (localId = messageId)
                    updatedMessages.push({ ...receivedMsg, localId: receivedMsg.messageId });
                }

                if (receivedMsg.messageType === 'DELETED') {
                    updatedMessages = updatedMessages.map(msg => {
                        if (String(msg.parentMessageId) === receivedId) {
                            return { ...msg, parentMessageContent: "삭제된 메시지입니다." };
                        }
                        return msg;
                    });
                }
                return updatedMessages;
            });

            if (receivedMsg.senderId !== user.memberId) {
                markAsRead(roomId, user.memberId, receivedMsg.messageId).then(() => { loadChatRooms(); });
                if (!isUserAtBottomRef.current) {
                    addNotification(setIncomingNotifications, receivedMsg);
                }
            } else {
                if (!isUserAtBottomRef.current) {
                    addNotification(setOutgoingNotifications, receivedMsg);
                }
            }
            if (receivedMsg.messageType === 'NOTICE' || receivedMsg.type === 'NOTICE') { fetchRoomInfo(); }
        });

        const reactionSubscription = client.subscribe(`/topic/chat/room/${roomId}/reaction`, (message) => {
            const event = JSON.parse(message.body);
            if (event.type === 'REACTION_UPDATE') {
                setMessages(prev => {
                    const updatedId = String(event.messageId);
                    const existingIndex = prev.findIndex(msg => String(msg.messageId || msg.id) === updatedId);
                    if (existingIndex !== -1) {
                        const newMessages = [...prev];
                        const targetMsg = newMessages[existingIndex];
                        let newReactions = event.reactions || [];

                        if (String(event.reactorId) === String(user.memberId)) {
                            newReactions = newReactions.map(r => {
                                if (r.emojiType === event.emojiType) {
                                    if (event.action === 'ADD' || event.action === 'UPDATE') { return { ...r, selectedByMe: true }; }
                                    else if (event.action === 'REMOVE') { return { ...r, selectedByMe: false }; }
                                }
                                return { ...r, selectedByMe: false };
                            });
                        } else {
                            newReactions = newReactions.map(newR => {
                                const oldR = targetMsg.reactions?.find(o => o.emojiType === newR.emojiType);
                                return { ...newR, selectedByMe: oldR ? oldR.selectedByMe : false };
                            });
                        }
                        newMessages[existingIndex] = { ...targetMsg, reactions: newReactions };
                        return newMessages;
                    }
                    return prev;
                });
            }
        });

        const readSubscription = client.subscribe(`/topic/chat/room/${roomId}/read`, (message) => {
            const readEvent = JSON.parse(message.body);
            if (readEvent.type === 'READ_UPDATE') {
                setMessages(prev => prev.map(msg => {
                    if (readEvent.unreadCountMap && readEvent.unreadCountMap[msg.messageId] !== undefined) {
                        return { ...msg, unreadCount: readEvent.unreadCountMap[msg.messageId] };
                    }
                    return msg;
                }));
            }
        });

        // 전송 에러 및 개인 알림 구독
        const userSubscription = client.subscribe(`/topic/user/${user.memberId}`, (message) => {
            try {
                const receivedMsg = JSON.parse(message.body);
                // 현재 채팅방 관련 에러인지 확인
                if (receivedMsg.messageType === 'ERROR' && String(receivedMsg.chatRoomId) === String(roomId)) {
                    console.error("채팅 오류 수신:", receivedMsg.content);
                    showAlert(receivedMsg.content, "전송 실패");

                    // 낙관적 메시지 롤백
                    setMessages(prev => {
                        const newMessages = [...prev];
                        // 뒤에서부터 검색하여 가장 최근의 낙관적 메시지를 찾음
                        for (let i = newMessages.length - 1; i >= 0; i--) {
                            if (newMessages[i].isOptimistic) {
                                console.log("🗑️ 전송 실패로 인한 낙관적 메시지 제거:", newMessages[i]);
                                newMessages.splice(i, 1);
                                break; // 하나만 제거
                            }
                        }
                        return newMessages;
                    });
                }
            } catch (e) {
                console.error("Error parsing user message", e);
            }
        });

        return () => {
            roomSubscription.unsubscribe();
            reactionSubscription.unsubscribe();
            readSubscription.unsubscribe();
            userSubscription.unsubscribe();
        };
    }, [roomId, client, connected, user?.memberId, showAlert]);

    // 무한 스크롤 옵저버 (상단 도달 시 이전 메시지 로드)
    const isInitialScrollComplete = useRef(false); // [Fix] 초기 스크롤 완료 여부 추적

    // messages를 ref에 동기화 (observer 클로저가 항상 최신 메시지 참조하도록)
    const messagesRef = useRef([]);
    useEffect(() => {
        messagesRef.current = messages;
    }, [messages]);

    const handleObserver = useCallback((entries) => {
        const target = entries[0];
        // ref로만 참조하여 클로저 stale 문제 방지
        if (
            target.isIntersecting &&
            hasMoreRef.current &&
            messagesRef.current.length > 0 &&
            isInitialScrollComplete.current
        ) {
            const firstMsgId = messagesRef.current[0].messageId;
            fetchMessages(firstMsgId);
        }
    }, [fetchMessages]); // fetchMessages만 의존 (안정적 - roomId/memberId 고정 시)

    useEffect(() => {
        const option = { root: null, rootMargin: "20px", threshold: 1.0 };
        const observer = new IntersectionObserver(handleObserver, option);
        if (observerTarget.current) observer.observe(observerTarget.current);
        return () => observer && observer.disconnect();
    }, [handleObserver]);

    const previousMessageCountRef = useRef(0);
    const messagesContainerRef = useRef(null);
    const isUserAtBottomRef = useRef(true);
    
    // 스크롤 위치 추적 (하단 여부 판별 및 알림 해제)
    useEffect(() => {
        const findMessageContainer = () => {
            const containers = document.querySelectorAll('[class*="messageList"]');
            return containers[0];
        };

        const container = findMessageContainer();
        if (!container) return;

        messagesContainerRef.current = container;

        const handleScroll = () => {
            const { scrollTop, scrollHeight, clientHeight } = container;
            // 하단에서 100px 이내면 하단으로 간주
            const isAtBottom = scrollHeight - scrollTop - clientHeight < 100;
            isUserAtBottomRef.current = isAtBottom;

            // 하단 도달 시 알림 해제
            if (isAtBottom) {
                setIncomingNotifications([]);
                setOutgoingNotifications([]);
            }
        };

        container.addEventListener('scroll', handleScroll);
        return () => container.removeEventListener('scroll', handleScroll);
    }, []);
    
    // 메시지 수 변경 시 자동 스크롤 (신규 메시지 알림)
    useEffect(() => {
        if (!messagesEndRef.current || messages.length === 0) return;
        
        // [Fix] isFirstLoad 분기 제거 - 초기 스크롤은 initializeRoom의 setTimeout에서 단독 처리
        // isInitialScrollComplete 이후 신규 메시지 수신 시에만 자동 하단 스크롤
        if (!isInitialScrollComplete.current) {
            // 초기 로드 중 - 메시지 카운터만 업데이트, 스크롤은 initializeRoom이 담당
            previousMessageCountRef.current = messages.length;
            isUserAtBottomRef.current = true;
        } else if (messages.length > previousMessageCountRef.current && isUserAtBottomRef.current) {
            // 이미 하단에 있을 때 신규 메시지 → 자동 하단 스크롤
            messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
            previousMessageCountRef.current = messages.length;
        } else if (messages.length > previousMessageCountRef.current) {
            previousMessageCountRef.current = messages.length;
        }
    }, [messages]);

    // 무한 스크롤 시 스크롤 위치 유지 (상단에 메시지 추가 후 위치 보정)
    useLayoutEffect(() => {
        if (prevScrollHeight.current > 0 && messagesContainerRef.current) {
            const container = messagesContainerRef.current;
            const newScrollHeight = container.scrollHeight;
            const diff = newScrollHeight - prevScrollHeight.current;

            if (diff > 0) {
                // 절대값(=diff)이 아닌 상대값(+=diff)으로 보정
                // → 현재 스크롤 위치에서 늘어난 높이만큼 아래로 이동하여 화면이 튀지 않음
                container.scrollTop += diff;
            }
            prevScrollHeight.current = 0; // Reset
        }
    }, [messages]);

    // 텍스트 메시지 전송 (낙관적 업데이트 후 WebSocket 발행)
    const handleSend = () => {
        if (!input.trim()) return;
        if (!connected) {
            showAlert("서버 연결이 끊어졌습니다. 잠시 후 다시 시도해주세요.");
            return;
        }

        const msgDto = {
            chatRoomId: roomId,
            senderId: user.memberId,
            content: input,
            messageType: 'TEXT',
            parentMessageId: replyTo ? replyTo.messageId : null
        };

        const tempId = Date.now();
        const optimisticMsg = {
            ...msgDto,
            messageId: tempId,
            localId: tempId,
            senderName: user.name || "나",
            senderProfileImage: user.profileImage,
            createdAt: new Date().toISOString(),
            isOptimistic: true,
            reactions: [],
            unreadCount: 0,
            parentMessageId: replyTo ? replyTo.messageId : null,
            parentMessageContent: replyTo ? replyTo.content : null,
            parentMessageSenderName: replyTo ? replyTo.senderName : null
        };

        setMessages(prev => [...prev, optimisticMsg]);

        try {
            client.publish({ destination: '/app/chat/message', body: JSON.stringify(msgDto) });
            setInput('');
            setReplyTo(null);
        } catch (error) {
            console.error("메시지 전송 실패", error);
            showAlert("메시지 전송에 실패했습니다.");
            setMessages(prev => prev.filter(msg => msg.messageId !== tempId));
        }
    };

    // 파일/이미지 메시지 전송
    const handleFileUpload = (fileUrl, type) => {
        if (!connected) {
            showAlert("서버 연결이 끊어졌습니다. 잠시 후 다시 시도해주세요.");
            return;
        }

        const msgDto = {
            chatRoomId: roomId,
            senderId: user.memberId,
            content: fileUrl,
            messageType: type, // IMAGE or FILE
            parentMessageId: replyTo ? replyTo.messageId : null
        };

        client.publish({ destination: '/app/chat/message', body: JSON.stringify(msgDto) });
        setReplyTo(null);
    };

    // [추가] 퇴장 중인지 여부 (퇴장 직후 추가 API 호출 방지 가드)
    const isLeaving = useRef(false);

    // 채팅방 나가기
    const handleLeave = () => {
        showConfirm("정말 채팅방을 나가시겠습니까?", async () => {
            try {
                isLeaving.current = true; // 가드 활성화
                await leaveChatRoom(roomId, user.memberId);
                // 목록 갱신 후 이동
                await loadChatRooms();
                navigate('/chat', { replace: true });
            } catch (error) {
                isLeaving.current = false; // 에러 시 가드 해제
                console.error("채팅방 나가기 실패", error);
                showAlert(error.response?.data || "나가기에 실패했습니다.");
            }
        });
    };

    // Enter 키 전송 (Shift+Enter 줄바꿈, 한글 조합 중 제외)
    const handleKeyDown = (e) => {
        if (e.nativeEvent.isComposing) return;
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            handleSend();
        }
    };

    useEffect(() => {
        if (showMemberModal && roomId && !isLeaving.current) {
            getChatRoomUsers(roomId).then(data => setRoomMembers(data)).catch(err => console.error("멤버 조회 실패", err));
        }
    }, [showMemberModal, roomId]);

    // 채팅방 공지 설정
    const handleSetNotice = useCallback(async (message) => {
        try {
            await setNotice(roomId, user.memberId, message.messageId);
        } catch (error) {
            console.error("공지 설정 실패", error);
            showAlert("공지 설정에 실패했습니다.");
        }
    }, [roomId, user?.memberId, showAlert]);

    // 채팅방 공지 해제
    const handleClearNotice = useCallback(async () => {
        try {
            await clearNotice(roomId, user.memberId);
        } catch (error) {
            console.error("공지 해제 실패", error);
            showAlert("공지 해제에 실패했습니다.");
        }
    }, [roomId, user?.memberId, showAlert]);

    // 방 정보 및 메시지 새로고침
    const handleRefresh = useCallback(() => { fetchRoomInfo(); fetchMessages(0); }, [fetchRoomInfo, fetchMessages]);

    // 이미지 로드 후 하단 스크롤 유지
    const handleImageLoad = useCallback(() => { 
        if (isUserAtBottomRef.current) {
            messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
        }
    }, []);

    const searchInputRef = useRef(null);

    useEffect(() => {
        if (showSearch && searchInputRef.current) {
            searchInputRef.current.focus();
        }
    }, [showSearch]);

    // 메시지 검색
    const handleSearch = async () => {
        if (!searchKeyword.trim()) {
            showAlert("검색어를 입력해주세요.");
            return;
        }

        try {
            // 처음 10개 검색
            const results = await searchMessages(roomId, user.memberId, searchKeyword, 10, 0);
            console.log('🔍 Search results:', results);
            if (results.length === 0) {
                showAlert("검색 결과가 없습니다.");
                setSearchResults([]);
                setCurrentSearchIndex(-1);
                setSearchOffset(0);
                setHasMoreSearchResults(false);
                return;
            }

            setSearchResults(results);
            setSearchOffset(results.length);
            setHasMoreSearchResults(results.length === 10); // 10개면 더 있을 수 있음

            // 가장 최근 결과 (인덱스 0)로 이동
            setCurrentSearchIndex(0);
            console.log('🔍 First result messageId:', results[0].messageId);
            scrollToSearchResult(results[0].messageId);
        } catch (error) {
            console.error("검색 실패", error);
            showAlert("검색 중 오류가 발생했습니다.");
        }
    };

    // 검색 결과로 스크롤 — DOM에 없으면 fetchMessages로 로드 후 이동 (방법 2)
    const scrollToSearchResult = useCallback(async (messageId) => {
        // 1. 먼저 DOM에서 탐색
        let element = document.querySelector(`[data-message-id="${messageId}"]`);

        if (!element && hasMoreRef.current) {
            // 2. DOM에 없으면 fetchMessages를 반복 호출하여 위로 로드
            let found = false;
            const MAX_FETCH = 10; // 최대 10번(300개) 로드 시도
            for (let i = 0; i < MAX_FETCH; i++) {
                const firstMsgId = messagesRef.current[0]?.messageId;
                if (!firstMsgId || !hasMoreRef.current) break;

                // 현재 스크롤 높이 저장 (무한스크롤 보정용)
                if (messagesContainerRef.current) {
                    prevScrollHeight.current = messagesContainerRef.current.scrollHeight;
                }
                await fetchMessages(firstMsgId);
                // 렌더 대기
                await new Promise(r => setTimeout(r, 120));

                element = document.querySelector(`[data-message-id="${messageId}"]`);
                if (element) { found = true; break; }
            }

            if (!found) {
                showAlert("해당 메시지를 찾을 수 없습니다.");
                return;
            }
        } else if (!element) {
            showAlert("해당 메시지를 찾을 수 없습니다.");
            return;
        }

        // 3. 하이라이트 + 스크롤
        setHighlightedMessageId(null);
        setTimeout(() => {
            setHighlightedMessageId(messageId);
            const el = document.querySelector(`[data-message-id="${messageId}"]`);
            if (el) el.scrollIntoView({ behavior: 'smooth', block: 'center' });

            if (highlightTimeoutRef.current) clearTimeout(highlightTimeoutRef.current);
            highlightTimeoutRef.current = setTimeout(() => {
                setHighlightedMessageId(null);
                highlightTimeoutRef.current = null;
            }, 3000);
        }, 50);
    }, [fetchMessages, showAlert]);

    // 이전 검색 결과로 이동 (더 오래된 방향)
    const handlePrevSearchResult = async () => {
        console.log('◀ Prev button clicked, currentIndex:', currentSearchIndex, 'total:', searchResults.length);
        if (searchResults.length === 0) return;

        // 더 오래된 결과로 이동
        const newIndex = currentSearchIndex + 1;
        console.log('◀ New index will be:', newIndex);

        // 현재 배열의 마지막에 도달하면 다음 10개 로드
        if (newIndex >= searchResults.length && hasMoreSearchResults) {
            console.log('◀ Loading more results, offset:', searchOffset);
            try {
                const nextResults = await searchMessages(roomId, user.memberId, searchKeyword, 10, searchOffset);
                console.log('◀ Loaded additional results:', nextResults.length);
                if (nextResults.length > 0) {
                    const updatedResults = [...searchResults, ...nextResults];
                    setSearchResults(updatedResults);
                    setSearchOffset(prev => prev + nextResults.length);
                    setHasMoreSearchResults(nextResults.length === 10);

                    setCurrentSearchIndex(newIndex);
                    // updatedResults 배열에서 newIndex 위치의 messageId 사용
                    scrollToSearchResult(updatedResults[newIndex].messageId);
                }
            } catch (error) {
                console.error("추가 검색 실패", error);
            }
        } else if (newIndex < searchResults.length) {
            console.log('◀ Navigating to existing result at index:', newIndex);
            setCurrentSearchIndex(newIndex);
            scrollToSearchResult(searchResults[newIndex].messageId);
        }
    };

    // 메시지 스크롤 및 하이라이트 공통 함수
    const highlightTimeoutRef = useRef(null);

    const scrollToMessage = useCallback((messageId) => {
        console.log("📜 스크롤 시도: messageId =", messageId);

        if (highlightTimeoutRef.current) {
            clearTimeout(highlightTimeoutRef.current);
        }

        // ✨ [Fix] 연속 클릭 시 재시동: 먼저 null로 초기화해야 React가 변경을 감지함
        setHighlightedMessageId(null);

        setTimeout(() => {
            setHighlightedMessageId(messageId);

            const element = document.querySelector(`[data-message-id="${messageId}"]`);
            if (element) {
                console.log("✅ 요소 찾음, 스크롤 실행");
                element.scrollIntoView({ behavior: 'smooth', block: 'center' });
            } else {
                console.warn("❌ 메시지를 찾을 수 없습니다 (DOM 미존재):", messageId);
                showAlert("해당 메시지를 찾을 수 없습니다. (스크롤 위쪽에 있을 수 있습니다)");
            }

            // 3초 후 하이라이트 해제
            highlightTimeoutRef.current = setTimeout(() => {
                setHighlightedMessageId(null);
                highlightTimeoutRef.current = null;
            }, 3000);
        }, 50);
    }, [showAlert]);

    // 다음 검색 결과로 이동 (더 최신 방향)
    const handleNextSearchResult = () => {
        console.log('Next button clicked, currentIndex:', currentSearchIndex);
        if (searchResults.length === 0) return;

        // 더 최근 결과로 이동 (인덱스 감소)
        if (currentSearchIndex > 0) {
            const newIndex = currentSearchIndex - 1;
            console.log('▶ Navigating to index:', newIndex);
            setCurrentSearchIndex(newIndex);
            scrollToSearchResult(searchResults[newIndex].messageId);
        } else {
            console.log('▶ Already at most recent result (index 0)');
        }
    };

    // 검색창 닫기 및 초기화
    const handleCloseSearch = () => {
        setShowSearch(false);
        setSearchResults([]);
        setSearchKeyword('');
    };

    // 답장 클릭 시 원본 메시지로 스크롤
    const handleReplyClick = (targetMessageId) => {
        if (!targetMessageId) return;
        scrollToMessage(targetMessageId);
    };


    // 상대방 정보 추출 (1:1 채팅용)
    const getOtherMember = () => {
        if (roomInfo.roomType === 'SINGLE' && roomInfo.participants && user) {
            return roomInfo.participants.find(p => String(p.memberId) !== String(user.memberId));
        }
        return null;
    };

    const otherMember = getOtherMember();

    // 화면에 표시할 이미지 URL 결정
    const displayRoomImage = (() => {
        if (roomInfo.roomType === 'SINGLE') {
            // 1순위: participants에서 찾은 상대방 프사
            if (otherMember?.profileImageUrl) return getFullUrl(otherMember.profileImageUrl);
            // 2순위: roomInfo에 이미 있다면 사용 (목록 등에서 넘어온 경우)
            if (roomInfo.otherMemberProfile) return getFullUrl(roomInfo.otherMemberProfile);
            return "/default-profile.svg";
        } else {
            // GROUP
            return getFullUrl(roomInfo.roomImage) || "/default-room.svg";
        }
    })();

    // 화면에 표시할 제목 결정
    const displayTitle = (() => {
        if (roomInfo.title) return roomInfo.title;
        if (roomInfo.roomType === 'SINGLE') {
            return otherMember?.memberName || roomInfo.otherMemberName || "알 수 없는 대화방";
        }
        return "그룹 채팅";
    })();

    const isAnyModalOpen = showMemberModal || showProfileModal || modalConfig.isOpen;

    return (
        <div className={styles.container}>
            {/* Header */}
            <div className={styles.header}>
                {/* ✨ Header Image */}
                <div className={styles.headerImage}>
                    <img
                        src={displayRoomImage}
                        alt="Room"
                        className={styles.roomImg}
                        onError={(e) => { e.target.src = roomInfo.roomType === 'SINGLE' ? "/default-profile.svg" : "/default-room.svg"; }}
                    />
                </div>
                <h3 className={styles.title}>
                    {displayTitle}
                </h3>
                <div className={styles.actions} ref={menuRef}>
                    <button
                        className={`${styles.menuBtn} ${showMenu ? styles.active : ''}`}
                        onClick={() => setShowMenu(!showMenu)}
                        title="더보기"
                    >
                        ⋮
                    </button>

                    {showMenu && (
                        <div className={styles.dropdownMenu}>
                            <button
                                className={styles.menuItem}
                                onClick={() => {
                                    setShowSearch(!showSearch);
                                    setShowMenu(false);
                                    // ✨ 검색창 열리면 포커스 (useEffect로 처리되지만 명시적으로도 좋음)
                                    if (!showSearch) setTimeout(() => searchInputRef.current?.focus(), 100);
                                }}
                            >
                                <span>🔍</span> 메시지 검색
                            </button>

                            {roomInfo.roomType !== 'SINGLE' && (
                                <button
                                    className={styles.menuItem}
                                    onClick={() => {
                                        setShowMemberModal(true);
                                        setShowMenu(false);
                                    }}
                                >
                                    <span>⚙️</span> 채팅방 설정
                                </button>
                            )}

                            <button
                                className={`${styles.menuItem} ${styles.danger}`}
                                onClick={() => {
                                    handleLeave();
                                    setShowMenu(false);
                                }}
                            >
                                <span>🚪</span> 나가기
                            </button>
                        </div>
                    )}
                </div>
            </div>

            {/* 공지 배너 */}
            {roomInfo.noticeContent && (
                <div className={styles.noticeBanner}>
                    <div 
                        className={styles.noticeContentWrapper} 
                        onClick={() => scrollToMessage(roomInfo.noticeMessageId)}
                        title="공지 메시지로 이동"
                    >
                        <span className={styles.noticeIcon}>📢</span>
                        <div className={styles.noticeTextContainer}>
                            <span className={styles.noticeText}>{extractOriginalFileName(roomInfo.noticeContent)}</span>
                            {roomInfo.noticeSenderName && (
                                <span className={styles.noticeSender}> - {roomInfo.noticeSenderName}</span>
                            )}
                        </div>
                    </div>
                    <button onClick={handleClearNotice} className={styles.noticeCloseBtn} title="공지 내리기">✖</button>
                </div>
            )}

            {/* 메시지 검색 바 */}
            {showSearch && (
                <div className={styles.searchBar}>
                    <input
                        type="text"
                        ref={searchInputRef}
                        value={searchKeyword}
                        onChange={(e) => setSearchKeyword(e.target.value)}
                        onKeyDown={(e) => {
                            if (e.key === 'Enter' && !e.nativeEvent.isComposing) handleSearch();
                        }}
                        placeholder="메시지 검색..."
                        className={styles.searchInput}
                    />
                    <button onClick={handleSearch} className={styles.searchButton}>검색</button>
                    {searchResults.length > 0 && (
                        <>
                            <button onClick={handleNextSearchResult} className={styles.navButton} title="최신으로">◀</button>
                            <span className={styles.searchCount}>
                                {currentSearchIndex + 1} / {hasMoreSearchResults ? `${searchResults.length}+` : searchResults.length}
                            </span>
                            <button onClick={handlePrevSearchResult} className={styles.navButton} title="과거로">▶</button>
                        </>
                    )}
                    <button onClick={handleCloseSearch} className={styles.closeSearchButton} title="닫기">✖</button>
                </div>
            )}



            {/* 최근 읽은 메시지 이동 버튼 */}
            {showLastReadBtn && lastReadMarkerId && (
                <button
                    className={styles.lastReadBtn}
                    onClick={() => {
                        const markerEl = document.getElementById(`read-marker-${lastReadMarkerId}`);
                        if (markerEl) {
                            markerEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
                        }
                        setShowLastReadBtn(false);
                    }}
                >
                    📌 마지막으로 읽은 메시지로 이동
                </button>
            )}

            {/* Message List */}
            <div className={styles.messageList} style={{ visibility: isReady ? 'visible' : 'hidden' }}>
                {/* overflow-anchor:none → 브라우저 스크롤 앵커가 이 div에 걸리지 않도록 */}
                <div ref={observerTarget} style={{ height: '10px', overflowAnchor: 'none' }} />
                {messages.map((msg, index) => {
                    const currentDate = new Date(msg.createdAt).toDateString();
                    const prevDate = index > 0 ? new Date(messages[index - 1].createdAt).toDateString() : null;
                    const isNewDate = currentDate !== prevDate;

                    return (
                        <React.Fragment key={msg.localId || msg.messageId || index}>
                            {isNewDate && (
                                <div className={styles.dateSeparator}>
                                    <span>
                                        {(() => {
                                            try {
                                                const date = new Date(msg.createdAt);
                                                return isNaN(date.getTime())
                                                    ? ""
                                                    : date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' });
                                            } catch (e) {
                                                return "";
                                            }
                                        })()}
                                    </span>
                                </div>
                            )}
                            <div data-message-id={msg.messageId || msg.id}>
                                <MessageBubble
                                    message={msg}
                                    onReply={setReplyTo}
                                    onSetNotice={handleSetNotice}
                                    isOwner={roomInfo.roomType === 'SINGLE' || String(roomInfo.creatorId) === String(user.memberId)}
                                    onRefresh={handleRefresh}
                                    onImageLoad={handleImageLoad}
                                    isHighlighted={highlightedMessageId === (msg.messageId || msg.id)}
                                    // ✨ 하이라이트 여부 전달
                                    showAlert={showAlert} // Pass showAlert
                                    onReplyClick={scrollToMessage} // ✨ 답장 클릭 핸들러 전달
                                />
                            </div>
                            
                            {/* ✨ 마지막 읽은 위치 구분선 */}
                            {String(msg.messageId || msg.id) === String(lastReadMarkerId) && (
                                <div id={`read-marker-${lastReadMarkerId}`} className={styles.readDivider}>
                                    <div className={styles.readDividerLine}></div>
                                    <span>여기까지 읽었습니다</span>
                                    <div className={styles.readDividerLine}></div>
                                </div>
                            )}
                        </React.Fragment>
                    );
                })}
                <div ref={messagesEndRef} />
            </div>

            {/* 입력 영역 */}
            <div className={styles.inputAreaWrapper}>
                {/* 수신 알림 토스트 (좌측) */}
                <div className={styles.notificationStackLeft}>
                    {incomingNotifications.map((noti) => (
                        <div
                            key={noti._id}
                            className={`${styles.newMessageNotification} ${noti.closing ? styles.fadeOut : ''}`}
                            onClick={() => {
                                messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
                                setIncomingNotifications([]); // Clear all on click (or filter)
                            }}
                        >
                            <div className={styles.notificationContent}>
                                <span className={styles.notificationSender}>{noti.senderName}</span>
                                <span className={styles.notificationText}>
                                    {(noti.contentType === 'IMAGE' || noti.messageType === 'IMAGE') ? '사진' :
                                        (noti.contentType === 'FILE' || noti.messageType === 'FILE') ? extractOriginalFileName(noti.content) :
                                            noti.content}
                                </span>
                            </div>
                        </div>
                    ))}
                </div>

                {/* 발신 알림 토스트 (우측) */}
                <div className={styles.notificationStackRight}>
                    {outgoingNotifications.map((noti) => (
                        <div
                            key={noti._id}
                            className={`${styles.newMessageNotification} ${styles.myNotification} ${noti.closing ? styles.fadeOut : ''}`}
                            onClick={() => {
                                messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
                                setOutgoingNotifications([]);
                            }}
                        >
                            <div className={styles.notificationContent}>
                                <span className={styles.notificationSender}>내 메시지</span>
                                <span className={styles.notificationText}>
                                    {(noti.contentType === 'IMAGE' || noti.messageType === 'IMAGE') ? '사진 보냄' :
                                        (noti.contentType === 'FILE' || noti.messageType === 'FILE') ? extractOriginalFileName(noti.content) :
                                            noti.content}
                                </span>
                            </div>
                        </div>
                    ))}
                </div>
                {replyTo && (
                    <div className={styles.replyBanner}>
                        <div className={styles.replyInfo}>
                            <span className={styles.replyToName}>To. {replyTo.senderName || "알 수 없음"}</span>
                            <span className={styles.replyToContent}>
                                {replyTo.content ? extractOriginalFileName(replyTo.content) : "내용 없음"}
                            </span>
                        </div>
                        <button onClick={() => setReplyTo(null)} className={styles.replyCloseBtn}>X</button>
                    </div>
                )}

                <div className={styles.inputArea}>
                    <FileUploadButton onFileUploaded={handleFileUpload} showAlert={showAlert} />
                    <textarea 
                        ref={chatInputRef}
                        className={styles.input}
                        value={input}
                        onChange={(e) => setInput(e.target.value)}
                        onKeyDown={handleKeyDown}
                        placeholder={
                            isAnyModalOpen 
                            ? "" 
                            : (replyTo ? `${replyTo.senderName}님에게 답장...` : "메시지를 입력하세요...")
                        }
                        rows={1}
                        disabled={isAnyModalOpen}
                    />
                    <button className={styles.sendBtn} onClick={handleSend} disabled={!input.trim() || isAnyModalOpen}>
                        전송
                    </button>
                </div>
            </div>

            {/* 1:1 채팅 상대방 프로필 모달 */}
            {showProfileModal && roomInfo.roomType === 'SINGLE' && (
                <UserDatailModal
                    isOpen={showProfileModal}
                    onClose={() => {
                        setShowProfileModal(false);
                        if (chatInputRef.current) chatInputRef.current.focus();
                    }}
                    memberId={roomInfo.otherMemberId}
                    zIndex={15000}
                />
            )}

            {/* 채팅방 멤버 관리 모달 */}
            {showMemberModal && (
                <MemberManagementModal
                    onClose={() => {
                        setShowMemberModal(false);
                        if (chatInputRef.current) chatInputRef.current.focus();
                    }}
                    roomId={roomId}
                    currentRoomTitle={roomInfo.title}
                    currentRoomImage={roomInfo.roomImage}
                    roomType={roomInfo.roomType}
                    currentMembers={roomMembers}
                    currentUserId={user.memberId}
                    isOwner={roomMembers.find(m => String(m.memberId) === String(user.memberId))?.role === 'OWNER'}
                    showAlert={showAlert}
                    showConfirm={showConfirm}
                    onMemberUpdate={() => {
                        getChatRoomUsers(roomId).then(data => setRoomMembers(data));
                    }}
                    isAlertOpen={modalConfig.isOpen} // Alert 상태 전달
                />
            )}

            <CustomModal
                isOpen={modalConfig.isOpen}
                onClose={modalConfig.onCancel}
                title={modalConfig.title}
                message={modalConfig.message}
                type={modalConfig.type}
                onConfirm={modalConfig.onConfirm}
                onCancel={modalConfig.onCancel}
                zIndex={12000}
            />
        </div>
    );
};

export default ChatRoomDetail;
