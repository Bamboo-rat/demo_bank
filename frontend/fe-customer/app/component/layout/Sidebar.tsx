import React, { useState } from 'react'
import menuData from './menu.json'
import { Link } from 'react-router'
import logo from '/app/assets/images/logo-kienlongbank.png'

interface SidebarProps {
  onToggle?: (isCollapsed: boolean) => void
}

const Sidebar: React.FC<SidebarProps> = ({ onToggle }) => {
  const [activeMenu, setActiveMenu] = useState<number | null>(null)
  const [expandedMenus, setExpandedMenus] = useState<Set<number>>(new Set())
  const [isCollapsed, setIsCollapsed] = useState(false)

  const toggleSubMenu = (menuId: number) => {
    const newExpanded = new Set(expandedMenus)
    if (newExpanded.has(menuId)) {
      newExpanded.delete(menuId)
    } else {
      newExpanded.add(menuId)
    }
    setExpandedMenus(newExpanded)
  }

  const handleMenuClick = (menuId: number, hasSubMenu: boolean) => {
    if (hasSubMenu) {
      toggleSubMenu(menuId)
    } else {
      setActiveMenu(menuId)
    }
  }

  const toggleSidebar = () => {
    const newCollapsedState = !isCollapsed
    setIsCollapsed(newCollapsedState)
    onToggle?.(newCollapsedState)
  }

  return (
    <aside className={`h-screen bg-linear-to-b from-blue-50 to-blue-25 border-r border-blue-200 flex flex-col transition-all duration-300 relative ${
      isCollapsed ? 'w-20' : 'w-72'
    }`}>
      <div className="p-4 border-b border-blue-100 bg-white/70 backdrop-blur-sm min-h-20 flex items-center shrink-0">
        <div className={`flex items-center gap-3 transition-all duration-200 ${
          isCollapsed ? 'w-full justify-center' : 'w-full'
        }`}>
          <div className="w-12 h-12 rounded-xl border-2 border-blue-300 bg-white flex items-center justify-center shadow-sm shrink-0">
            <img 
              src={logo} 
              alt="KLB" 
              className="w-8 h-8 object-contain"
            />
          </div>
          {!isCollapsed && (
            <div className="flex-1 min-w-0">
              <h1 className="text-lg font-semibold text-blue-900 truncate">KienLong Bank</h1>
              <p className="text-sm text-blue-600/70 truncate">Digital Banking</p>
            </div>
          )}
        </div>
      </div>

      <button
        onClick={toggleSidebar}
        className="absolute -right-4 top-8 w-8 h-8 bg-blue-500 border-2 border-white rounded-full flex items-center justify-center shadow-lg hover:bg-blue-600 transition-all duration-200 z-50"
      >
        <span className={`material-icons-round text-white text-base transition-transform duration-300 ${
          isCollapsed ? 'rotate-180' : ''
        }`}>
          chevron_left
        </span>
      </button>

      <nav className={`flex-1 overflow-y-auto overflow-x-hidden p-4 space-y-2 ${
        isCollapsed ? 'px-3' : ''
      }`}>
        {menuData.mainMenu.map((menu) => {
          const hasSubMenu = menu.subMenu && menu.subMenu.length > 0
          const isExpanded = expandedMenus.has(menu.id)
          const isActive = activeMenu === menu.id

          return (
            <div key={menu.id} className="relative">
              <button
                onClick={() => handleMenuClick(menu.id, hasSubMenu)}
                className={`w-full flex items-center transition-all duration-200 group ${
                  isCollapsed 
                    ? 'justify-center px-3 py-4 rounded-xl' 
                    : 'justify-between px-4 py-3 rounded-lg'
                } ${
                  isActive
                    ? 'bg-blue-100/80 text-blue-700 shadow-sm border border-blue-200/50'
                    : 'text-blue-900/80 hover:bg-white/80 hover:shadow-sm hover:border hover:border-blue-100/50'
                }`}
              >
                <div className={`flex items-center ${
                  isCollapsed ? 'gap-0' : 'gap-4'
                }`}>
                  <span className={`material-icons-round transition-colors duration-200 ${
                    isCollapsed ? 'text-2xl' : 'text-xl'
                  } ${
                    isActive ? 'text-blue-600' : 'text-blue-500/70 group-hover:text-blue-600'
                  }`}>
                    {menu.icon}
                  </span>
                  {!isCollapsed && (
                    <span className="text-base font-medium">{menu.title}</span>
                  )}
                </div>
                
                {hasSubMenu && !isCollapsed && (
                  <span className={`material-icons-round text-lg text-blue-400 transition-transform duration-200 ${
                    isExpanded ? 'rotate-180' : ''
                  }`}>
                    expand_more
                  </span>
                )}
              </button>

              {hasSubMenu && isExpanded && !isCollapsed && (
                <div className="mt-2 ml-4 pl-6 border-l-2 border-blue-300/30 space-y-1 animate-slideDown">
                  {menu.subMenu.map((subItem, idx) => (
                    <Link
                      key={idx}
                      to={subItem.link}
                      className="block px-4 py-2.5 text-base text-blue-900/70 hover:text-blue-700 hover:bg-white/60 rounded-lg transition-all duration-200 hover:translate-x-1 border border-transparent hover:border-blue-100"
                    >
                      {subItem.title}
                    </Link>
                  ))}
                </div>
              )}

              {isCollapsed && (
                <div className="absolute left-full top-1/2 transform -translate-y-1/2 z-50">
                  <div className="ml-2 px-3 py-2 bg-blue-900 text-white text-sm rounded-lg opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none whitespace-nowrap shadow-lg">
                    {menu.title}
                    <div className="absolute right-full top-1/2 transform -translate-y-1/2 border-4 border-transparent border-r-blue-900"></div>
                  </div>
                </div>
              )}
            </div>
          )
        })}
      </nav>

      {/* Footer */}
      <div className={`p-4 border-t border-blue-100 bg-white/50 transition-all duration-200 shrink-0 ${
        isCollapsed ? 'text-center' : ''
      }`}>
        {!isCollapsed ? (
          <>
            <p className="text-sm text-blue-700/60 font-medium">© 2025 KienLong Bank</p>
            <p className="text-xs text-blue-600/50 mt-1">Secure • Reliable • Innovative</p>
          </>
        ) : (
          <div className="w-10 h-10 mx-auto rounded-lg border-2 border-blue-300 bg-white flex items-center justify-center shadow-sm">
            <span className="text-blue-600 text-xs font-bold">KLB</span>
          </div>
        )}
      </div>
    </aside>
  )
}

export default Sidebar