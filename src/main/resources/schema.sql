-- ============================================================
-- Схема БД: Фирма-сборщик
-- PostgreSQL 13+
-- ============================================================

-- DROP в обратном порядке
DROP TABLE IF EXISTS order_components   CASCADE;
DROP TABLE IF EXISTS order_products     CASCADE;
DROP TABLE IF EXISTS orders             CASCADE;
DROP TABLE IF EXISTS product_components CASCADE;
DROP TABLE IF EXISTS supplies           CASCADE;
DROP TABLE IF EXISTS sales_agents       CASCADE;
DROP TABLE IF EXISTS customers          CASCADE;
DROP TABLE IF EXISTS products           CASCADE;
DROP TABLE IF EXISTS components         CASCADE;
DROP TABLE IF EXISTS suppliers          CASCADE;

-- 1. Поставщики
CREATE TABLE suppliers (
    supplier_id   SERIAL        PRIMARY KEY,
    name          VARCHAR(255)  NOT NULL,
    address       VARCHAR(255),
    phone         VARCHAR(20)
);

-- 2. Компоненты
CREATE TABLE components (
    component_id      SERIAL       PRIMARY KEY,
    name              VARCHAR(255) NOT NULL,
    manufacturer      VARCHAR(255) NOT NULL,
    current_quantity  INT          NOT NULL DEFAULT 0 CHECK (current_quantity >= 0),
    min_stock         INT          NOT NULL DEFAULT 0 CHECK (min_stock        >= 0),
    UNIQUE (name, manufacturer)
);

-- 3. Изделия
CREATE TABLE products (
    product_id     SERIAL       PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    description    TEXT,
    assembly_days  INT          NOT NULL CHECK (assembly_days >= 0)
);

-- 4. Клиенты
CREATE TABLE customers (
    customer_id        SERIAL       PRIMARY KEY,
    organization_name  VARCHAR(255) NOT NULL,
    representative     VARCHAR(255) NOT NULL,
    contact_info       VARCHAR(255)
);

-- 5. Торговые агенты
CREATE TABLE sales_agents (
    agent_id      SERIAL       PRIMARY KEY,
    full_name     VARCHAR(255) NOT NULL,
    email         VARCHAR(100),
    pager_number  VARCHAR(50),
    phone_number  VARCHAR(20),
    pbx_code      VARCHAR(10)
);

-- 6. Поставки (M:N supplier <-> component)
CREATE TABLE supplies (
    supply_id        SERIAL        PRIMARY KEY,
    supplier_id      INT           NOT NULL REFERENCES suppliers (supplier_id)   ON DELETE RESTRICT,
    component_id     INT           NOT NULL REFERENCES components (component_id) ON DELETE RESTRICT,
    supply_date      DATE          NOT NULL,
    volume           INT           NOT NULL CHECK (volume > 0),
    purchase_price   DECIMAL(10,2) NOT NULL CHECK (purchase_price >= 0),
    debt             DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (debt >= 0)
);

-- 7. Состав изделия (M:N product <-> component)
CREATE TABLE product_components (
    product_id    INT NOT NULL REFERENCES products   (product_id)   ON DELETE CASCADE,
    component_id  INT NOT NULL REFERENCES components (component_id) ON DELETE RESTRICT,
    quantity      INT NOT NULL CHECK (quantity > 0),
    PRIMARY KEY (product_id, component_id)
);

-- 8. Заказы
CREATE TABLE orders (
    order_id     SERIAL        PRIMARY KEY,
    customer_id  INT           NOT NULL REFERENCES customers    (customer_id) ON DELETE RESTRICT,
    agent_id     INT                    REFERENCES sales_agents (agent_id)    ON DELETE SET NULL,
    order_date   DATE          NOT NULL DEFAULT CURRENT_DATE,
    total_sum    DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (total_sum >= 0)
);

-- 9. Изделия в заказе
CREATE TABLE order_products (
    order_id    INT           NOT NULL REFERENCES orders   (order_id)   ON DELETE CASCADE,
    product_id  INT           NOT NULL REFERENCES products (product_id) ON DELETE RESTRICT,
    quantity    INT           NOT NULL CHECK (quantity > 0),
    price       DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    PRIMARY KEY (order_id, product_id)
);

-- 10. Компоненты в заказе
CREATE TABLE order_components (
    order_id      INT           NOT NULL REFERENCES orders     (order_id)     ON DELETE CASCADE,
    component_id  INT           NOT NULL REFERENCES components (component_id) ON DELETE RESTRICT,
    quantity      INT           NOT NULL CHECK (quantity > 0),
    price         DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    PRIMARY KEY (order_id, component_id)
);

-- Индексы
CREATE INDEX idx_supplies_supplier   ON supplies          (supplier_id);
CREATE INDEX idx_supplies_component  ON supplies          (component_id);
CREATE INDEX idx_pc_component        ON product_components(component_id);
CREATE INDEX idx_orders_customer     ON orders            (customer_id);
CREATE INDEX idx_orders_agent        ON orders            (agent_id);
CREATE INDEX idx_op_product          ON order_products    (product_id);
CREATE INDEX idx_oc_component        ON order_components  (component_id);

-- Под обязательные запросы:
CREATE INDEX idx_products_assembly   ON products    (assembly_days);
CREATE INDEX idx_components_manuf    ON components  (manufacturer);
CREATE INDEX idx_agents_pbx          ON sales_agents(pbx_code);
CREATE INDEX idx_orders_date         ON orders      (order_date);
