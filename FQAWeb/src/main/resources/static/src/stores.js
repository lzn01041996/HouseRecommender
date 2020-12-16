const key = 'VUE-CHAT-v1';

// 虚拟数据
    var now = new Date();
    var data = {
        // 登录用户
        userInfo: {
            id: 1,
            name: '李小宁',
            img: 'dist/images/2.jpg'
        },
        
        // 用户列表
        userList: [
            {
                id: 1,
                name: '李小宁',
                img: 'dist/images/2.png'
            },
            {
                id: 2,
                name: '站长素材',
                img: 'dist/images/2.png'
            },
            {
                id: 3,
                name: 'webpack',
                img: 'dist/images/3.jpg'
            }
        ],

        // 会话列表
        sessionList: [
            {
                userId: 2,
                messages: [
                    {
                        text: 'Hello，哈哈哈。',
                        date: now
                    }, 
                    {
                        text: '项目地址: https://sc.chinaz.com/jiaoben/',
                        date: now
                    },
                    {
                        text: '这是自己加的',
                        date: now
                    }
                ]
            },
            {
                userId: 3,
                messages: [
                    {
                        text: 'Hello，哈哈哈。',
                        date: now
                    },
                    {
                        text: '项目地址: https://sc.chinaz.com/jiaoben/',
                        date: now
                    }
                ]
            }
        ],
    };
    
    localStorage.setItem(key, JSON.stringify(data));


export default {
    fetch () {
        return JSON.parse(localStorage.getItem(key));
    },
    save (store) {
        localStorage.setItem(key, JSON.stringify(store));
    }
};