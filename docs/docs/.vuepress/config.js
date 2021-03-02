module.exports = {
    title: 'HOJ 文档',
    description: 'HOJ 的开发与使用文档',
    head: [
        ['link', { rel: 'icon', href: '/img/favicon.ico' }],
    ],
    base: '/',
    markdown: {
        lineNumbers: true // 代码块显示行号
    },
    themeConfig: {
        sidebarDepth: 5,
        nav: [
            { text: 'Demo', link: '' },
            { text: 'Gitee首页', link: 'https://gitee.com/himitzh0730/hoj' },
            { text: '作者首页', link: 'https://www.hcode.top/' },
        ],

        sidebar: [
            {
                title: '开始介绍',
                collapsable: true,
                children: [
                    '/introducition/',
                    '/introducition/about',
                ]
            },
			{
                title: '部署文档',
                collapsable: true,
                children: [
                    '/deploy/'
                ]
            },
			{
                title: '开发文档',
                collapsable: true,
                children: [
                    '/develop/'
                ]
            },
			{
                title: '使用文档',
                collapsable: true,
                children: [
                    '/use/'
                ]
            },
        ]

        
    },
    

}