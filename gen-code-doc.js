const fs = require('fs');
const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  AlignmentType, LevelFormat, HeadingLevel, BorderStyle, WidthType,
  ShadingType, VerticalAlign
} = require('docx');

const CN = { ascii: "Times New Roman", eastAsia: "宋体", hAnsi: "Times New Roman" };
const HEAD = { ascii: "Arial", eastAsia: "黑体", hAnsi: "Arial" };
const MONO = { ascii: "Consolas", eastAsia: "宋体", hAnsi: "Consolas" };

const h1 = (t) => new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: t, font: HEAD, bold: true })] });
const h2 = (t) => new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: t, font: HEAD, bold: true })] });
const p = (t, o = {}) => new Paragraph({ spacing: { after: 100, line: 340 }, children: [new TextRun({ text: t, font: CN, size: 23, ...o })] });
const bullet = (t) => new Paragraph({ numbering: { reference: "bullet", level: 0 }, spacing: { after: 50, line: 340 }, children: [new TextRun({ text: t, font: CN, size: 23 })] });
const codeBlock = (text) => text.split('\n').map(line => new Paragraph({ shading: { fill: "F5F5F5", type: ShadingType.CLEAR }, spacing: { after: 0, line: 276 }, indent: { left: 200 }, children: [new TextRun({ text: line || ' ', font: MONO, size: 20 })] }));

const table = (headers, rows, widths) => {
  const border = { style: BorderStyle.SINGLE, size: 1, color: "999999" };
  const cellBorders = { top: border, bottom: border, left: border, right: border };
  const total = widths.reduce((a, b) => a + b, 0);
  const scale = 9360 / total;
  widths = widths.map(w => Math.round(w * scale));
  const headerRow = new TableRow({ tableHeader: true, children: headers.map((h, i) => new TableCell({ borders: cellBorders, width: { size: widths[i], type: WidthType.DXA }, shading: { fill: "D5E8F0", type: ShadingType.CLEAR }, verticalAlign: VerticalAlign.CENTER, children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: h, font: CN, bold: true, size: 21 })] })] })) });
  const bodyRows = rows.map(row => new TableRow({ children: row.map((cell, i) => new TableCell({ borders: cellBorders, width: { size: widths[i], type: WidthType.DXA }, children: [new Paragraph({ children: [new TextRun({ text: cell, font: CN, size: 21 })] })] })) }));
  return new Table({ columnWidths: widths, rows: [headerRow, ...bodyRows] });
};

const children = [];

children.push(new Paragraph({ spacing: { before: 400, after: 200 }, alignment: AlignmentType.CENTER, children: [new TextRun({ text: "校园二手交易市场", font: HEAD, bold: true, size: 40 })] }));
children.push(new Paragraph({ spacing: { after: 100 }, alignment: AlignmentType.CENTER, children: [new TextRun({ text: "项目代码功能说明", font: HEAD, bold: true, size: 32 })] }));
children.push(new Paragraph({ spacing: { after: 400 }, alignment: AlignmentType.CENTER, children: [new TextRun({ text: "技术栈：Java 21 + Spring Boot 3.3 + MyBatis Plus + MySQL 8 + Vue 3 + Element Plus", font: CN, size: 21, color: "666666" })] }));

// ============ 一、项目概览 ============
children.push(h1("一、项目概览"));
children.push(p("本项目是一个前后端分离的校园二手交易市场系统。后端采用 Spring Boot 分层架构（Controller → Service → Mapper → Entity），前端采用 Vue 3 单页应用，前后端通过 RESTful API（JSON）通信，数据存储于 MySQL。"));
children.push(p("一次请求的完整链路：前端页面 → api/index.js（封装接口）→ request.js（带 token）→ 后端 Controller → Service → Mapper → MySQL，原路返回 JSON 给前端渲染。"));

// ============ 二、后端代码 ============
children.push(h1("二、后端代码"));

children.push(h2("2.1 启动类"));
children.push(table(["文件", "功能"], [["MarketApplication.java", "程序入口：@SpringBootApplication 启动 Spring Boot（组件扫描+自动配置），@MapperScan 扫描 Mapper 接口，@EnableScheduling 开启定时任务（7 天自动确认）"]], [2200, 7000]));

children.push(h2("2.2 Entity 实体层（14 个，对应 14 张表）"));
children.push(p("每个实体类都用 @TableName 映射表名、@TableId 标记主键自增、@Data 自动生成 getter/setter。字段名用驼峰，自动对应数据库下划线（如 realName → real_name）。"));
children.push(table(
  ["类", "对应表", "关键字段"],
  [
    ["User", "users", "username, password, realName, phone, email, city, gender, bankAccount, role(用户/商家/管理员), status(0待审核/1正常/2拒绝/3封禁), rating(买家好评率)"],
    ["MerchantProfile", "merchant_profiles", "shopName, businessLicense, idCard, level(1-5级), shopStatus(0正常/1下架/2封禁), banUntil, rating, totalSales"],
    ["Product", "products", "name, category, originalPrice, price, size, description, negotiable, stock, conditionLevel, status(待审核/在售/下架/锁定/售出), sales, rating"],
    ["ProductImage", "product_images", "productId, url, sortOrder"],
    ["CartItem", "cart_items", "userId, productId, quantity"],
    ["Order", "orders", "orderNo, userId(买家), merchantId(商家), totalAmount, status, createdAt, confirmedAt"],
    ["OrderItem", "order_items", "orderId, productId, quantity, price"],
    ["Wallet", "wallets", "userId, balance, points"],
    ["WalletTransaction", "wallet_transactions", "type(充值/支付/退款/收款), amount, orderId, description"],
    ["PointTransaction", "point_transactions", "type(获取/抵扣), points, description"],
    ["ProductReview", "product_reviews", "orderId, productId, userId, rating(1-5), content"],
    ["MerchantReview", "merchant_reviews", "userId, merchantId, orderId, rating, content"],
    ["BuyerReview", "buyer_reviews", "merchantId, userId(被评价买家), orderId, rating, content"],
    ["Blacklist", "blacklist", "userId(被拉黑), merchantId(拉黑方，NULL=平台), reason"]
  ],
  [1600, 2000, 5700]
));

children.push(h2("2.3 Mapper 数据访问层（14 个接口 + 1 个 XML）"));
children.push(p("14 个 Mapper 接口都继承 BaseMapper，基础增删改查免写 SQL。只有 ProductMapper 声明了两个自定义方法，SQL 写在 ProductMapper.xml 里："));
children.push(...codeBlock(`public interface ProductMapper extends BaseMapper<Product> {
    IPage<ProductVO> search(Page<ProductVO> page, String keyword, String category, String sortBy);
    ProductVO selectDetailWithShop(Long id);
}`));
children.push(p("ProductMapper.xml 是唯一的动态 SQL：用 <if> 按关键字模糊查询，用 <choose> 按价格/销量/好评率动态排序，用 LEFT JOIN 关联商家表拿店铺名和商家好评率。"));

children.push(h2("2.4 DTO 请求参数层（6 个，前端传给后端）"));
children.push(table(
  ["类", "用途", "字段"],
  [
    ["RegisterDTO", "普通用户注册", "username, password, realName, phone, email, city, gender, bankAccount, captchaId, captchaCode"],
    ["MerchantRegisterDTO", "商家注册", "上述字段 + shopName, businessLicense, idCard"],
    ["LoginDTO", "登录", "username, password"],
    ["ProductDTO", "发布/改商品", "name, category, originalPrice, price, size, description, negotiable, stock, conditionLevel, images"],
    ["OrderCreateDTO", "下单", "productId, quantity, cartItemIds"],
    ["ReviewDTO", "评价", "rating, content"]
  ],
  [2000, 1800, 5500]
));
children.push(p("作用：DTO 接收前端 JSON，用 @NotBlank、@Size 等注解做参数校验（如\"密码至少 6 位\"、\"银行账号 16 位\"）。"));

children.push(h2("2.5 VO 视图对象层（6 个，后端返回给前端）"));
children.push(table(
  ["类", "用途", "相比 Entity 多出的字段"],
  [
    ["ProductVO", "商品展示", "shopName(店铺名), merchantRating(商家好评率), images(图片列表)"],
    ["LoginVO", "登录返回", "token, user"],
    ["OrderVO", "订单展示", "shopName, items(明细含商品名/图片)"],
    ["CartItemVO", "购物车", "productName, image, price, shopName"],
    ["ReviewVO", "评价展示", "userName(评价人姓名)"],
    ["MerchantVO", "商家列表", "用户信息 + 店铺信息合并"]
  ],
  [1600, 1800, 5900]
));
children.push(p("作用：VO 把多张表的数据合并成一个对象，前端一次就能拿到所有要显示的信息。"));

children.push(h2("2.6 Service 业务层（7 个，核心业务逻辑）"));
children.push(p("AuthService（认证）：", { bold: true }));
children.push(bullet("getCaptcha()：用 Hutool 生成图片验证码，返回验证码 id + base64 图片"));
children.push(bullet("register(dto)：校验验证码、查重用户名、BCrypt 加密密码、创建待审核用户和钱包"));
children.push(bullet("merchantRegister(dto)：同上，额外创建商家资料（店铺名/营业执照/身份证）"));
children.push(bullet("login(dto)：校验密码、检查审核状态、签发 JWT、返回 token + 用户信息"));
children.push(p("ProductService（商品）：", { bold: true }));
children.push(bullet("search()：分页搜索 + 批量填图片"));
children.push(bullet("create()：校验店铺状态（封禁/下架不能发），创建待审核商品、保存图片"));
children.push(bullet("update() / offline() / listByMerchant() / listAll()：修改、下架、商家商品列表、管理端列表"));
children.push(p("CartService（购物车）：list / add / update / delete，加购时数量自动累加。", { bold: true }));
children.push(p("OrderService（订单/资金，最核心）：", { bold: true }));
children.push(bullet("create()：下单，支持单买或购物车批量，按商家自动拆单"));
children.push(bullet("confirm()：确认收货，结算货款给商家"));
children.push(bullet("requestReturn() / auditReturn()：申请退货 / 商家审核退货（退款+恢复库存+减销量）"));
children.push(bullet("autoConfirm()：定时任务，7 天未确认自动结算"));
children.push(bullet("内部 createSingleOrder()：黑名单校验、扣库存、加销量、扣买家款（资金托管）、送积分"));
children.push(bullet("内部 settle()：按商家等级算手续费，把钱打给商家"));
children.push(p("WalletService（钱包/积分）：getOrCreate、addBalance（余额变动+流水）、addPoints（积分变动+流水）、recharge（充值）、redeem（100 积分=1 元抵扣）、transactions（流水）。", { bold: true }));
children.push(p("ReviewService（评价）：", { bold: true }));
children.push(bullet("reviewProduct / reviewMerchant / reviewBuyer：商品、商家、买家三类评价"));
children.push(bullet("评价后自动重算对应对象的平均评分（买家评价转化为买家好评率）"));
children.push(p("AdminService（管理端）：listUsers、listMerchants、auditUser/auditMerchant/auditProduct（审核）、updateUser/deleteUser（改/删用户）、setLevel（等级）、banMerchant/unbanMerchant（封禁/解封）、offlineMerchant（下架）、recharge（充值）。", { bold: true }));

children.push(h2("2.7 Controller 接口层（9 个）"));
children.push(table(
  ["Controller", "提供的接口"],
  [
    ["AuthController", "验证码、注册、商家注册、登录、当前用户"],
    ["ProductController", "商品搜索、详情、发布、修改、下架、我的商品"],
    ["CartController", "购物车增删改查"],
    ["OrderController", "下单、订单列表、确认收货、退货、审核退货"],
    ["WalletController", "钱包余额、流水、积分抵扣"],
    ["ReviewController", "商品/商家/买家评价"],
    ["AdminController", "审核、充值、等级、封禁/解封/下架、修改/删除用户"],
    ["FileController", "图片上传"],
    ["MerchantController", "商家店铺信息"]
  ],
  [2400, 6900]
));
children.push(p("共同点：@RestController + @RequestMapping 定义路径，返回统一 Result，权限用 @RequireRole 标注。"));

children.push(h2("2.8 common / config / security 层"));
children.push(table(
  ["包", "类", "作用"],
  [
    ["common", "Result", "统一返回 {code, message, data}"],
    ["common", "BusinessException", "业务异常"],
    ["common", "GlobalExceptionHandler", "全局异常处理，统一转 JSON 错误"],
    ["config", "MybatisPlusConfig", "注册分页插件"],
    ["config", "WebConfig", "拦截器注册、跨域、/uploads 静态映射"],
    ["config", "DataInitializer", "启动时初始化管理员账号 admin/admin123"],
    ["security", "JwtUtil", "JWT 令牌签发与解析"],
    ["security", "UserContext", "ThreadLocal 存当前登录用户"],
    ["security", "RequireRole", "角色权限注解"],
    ["security", "AuthInterceptor", "登录拦截器（校验 JWT）"],
    ["security", "RequireRoleAspect", "AOP 权限切面"]
  ],
  [1400, 2800, 5000]
));

// ============ 三、前端代码 ============
children.push(h1("三、前端代码"));

children.push(h2("3.1 核心文件"));
children.push(table(
  ["文件", "功能"],
  [
    ["main.js", "入口，注册 Element Plus、Pinia、Router"],
    ["App.vue", "根组件，只有 <router-view>"],
    ["api/request.js", "Axios 实例 + 拦截器（自动带 token、统一处理错误和 401 跳转）"],
    ["api/index.js", "所有后端接口封装成函数"],
    ["store/auth.js", "Pinia 存登录用户信息"],
    ["router/index.js", "路由配置 + 路由守卫"],
    ["components/NavBar.vue", "顶部导航栏（按角色显示菜单）"]
  ],
  [2800, 6500]
));

children.push(h2("3.2 页面（9 个）"));
children.push(table(
  ["页面", "功能"],
  [
    ["Home.vue", "商品列表、搜索、排序、分页"],
    ["Login.vue", "登录"],
    ["Register.vue", "注册（验证码、商家注册切换）"],
    ["ProductDetail.vue", "商品详情、加购物车、立即购买、评价展示"],
    ["Cart.vue", "购物车、一键下单"],
    ["Orders.vue", "买家订单（筛选/收货/退货/评价）+ 销售订单（审核退货/评价买家）"],
    ["Wallet.vue", "钱包余额、积分抵扣、流水"],
    ["Merchant.vue", "店铺信息、我的商品（筛选/编辑/下架/看评价）、发布商品"],
    ["Admin.vue", "后台：用户/商家/商品管理、审核、封禁、充值"]
  ],
  [2600, 6700]
));

// ============ 四、资源文件 ============
children.push(h1("四、资源文件"));
children.push(table(
  ["文件", "作用"],
  [
    ["application.yml", "端口 8080、数据源连接、MyBatis Plus、JWT 密钥、上传路径"],
    ["schema.sql", "建 14 张表的建库脚本"],
    ["upgrade.sql", "数据库升级脚本（后加的 rating 字段、buyer_reviews 表）"],
    ["ProductMapper.xml", "商品搜索动态 SQL"]
  ],
  [2800, 6500]
));

// ============ 五、答辩重点 ============
children.push(h1("五、答辩重点（需要重点掌握）"));
children.push(bullet("OrderService：资金托管流程（下单扣款→托管→确认结算→扣手续费）、退货"));
children.push(bullet("RequireRoleAspect：AOP 权限校验（@RequireRole + 切面）"));
children.push(bullet("AuthInterceptor + JwtUtil：JWT 无状态鉴权"));
children.push(bullet("ProductMapper.xml：MyBatis Plus 动态 SQL"));
children.push(bullet("WebConfig：拦截器注册、跨域、静态资源映射"));
children.push(bullet("数据库设计：14 张表，为什么订单拆 orders + order_items、钱包拆 wallets + transactions"));

const doc = new Document({
  styles: {
    default: { document: { run: { font: CN, size: 23 } } },
    paragraphStyles: [
      { id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true, run: { size: 32, bold: true, color: "000000", font: HEAD }, paragraph: { spacing: { before: 240, after: 120 }, outlineLevel: 0 } },
      { id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true, run: { size: 27, bold: true, color: "000000", font: HEAD }, paragraph: { spacing: { before: 180, after: 100 }, outlineLevel: 1 } }
    ]
  },
  numbering: { config: [{ reference: "bullet", levels: [{ level: 0, format: LevelFormat.BULLET, text: "•", alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 720, hanging: 360 } } } }] }] },
  sections: [{ properties: { page: { margin: { top: 1440, right: 1440, bottom: 1440, left: 1440 } } }, children }]
});

Packer.toBuffer(doc).then(buffer => {
  fs.writeFileSync("d:/happy/软件开发实践大作业/项目代码功能说明.docx", buffer);
  console.log("OK 项目代码功能说明.docx");
});
