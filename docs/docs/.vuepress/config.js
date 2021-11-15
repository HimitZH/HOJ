module.exports = {
    title: 'HOJ 文档',
    description: 'HOJ 的开发与使用文档',
    head: [
        ['link', { rel: 'icon', href: '/img/favicon.ico' }],
    ],
    base: '/docs/',
    markdown: {
        lineNumbers: true // 代码块显示行号
    },
    themeConfig: {
        sidebarDepth: 5,
        nav: [
            { text: 'Demo', link: 'https://hdoi.cn/' },
            { text: 'Gitee', link: 'https://gitee.com/himitzh0730/hoj' },
			{ text: 'Github', link: 'https://github.com/HimitZH/HOJ' },
            { text: '作者首页', link: 'https://blog.csdn.net/weixin_43853097' },
        ],

        sidebar: [
            {
                title: '开始介绍',
                collapsable: true,
                children: [
                    'introducition/',
                    'introducition/about',
                ]
            },
			{
                title: '部署文档',
                collapsable: true,
                children: [
                    'deploy/',
					'deploy/docker',
					'deploy/mysql',
					'deploy/mysql-checker',
					'deploy/redis',
					'deploy/nacos',
                    'deploy/backend',
                    'deploy/judgeserver',
					'deploy/frontend',
                    'deploy/rsync'
                ]
            },
			{
                title: '开发文档',
                collapsable: true,
                children: [
                    'develop/',
                    'develop/db',
					'develop/sandbox'
                ]
            },
			{
                title: '使用文档',
                collapsable: true,
                children: [
                    'use/',
					'use/testcase',
					'use/contest',
					'use/import-problem',
					'use/import-user',
					'use/admin-user',
					'use/notice-announcement',
					'use/discussion-admin',
					'use/multi-judgeserver',
					'use/update-fe',
                    'use/spj'
                ]
            },
        ],
        
    },
    
}