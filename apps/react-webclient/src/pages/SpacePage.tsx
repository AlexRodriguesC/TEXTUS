import {useState} from "react";
import {
    Award,
    Bell,
    BookOpen,
    ChevronLeft,
    Dumbbell,
    Flame,
    Gem,
    Heart,
    Settings,
    ShoppingBag,
    Sparkles,
    Star,
    Target,
    Trophy,
    Users,
    Zap,
} from "lucide-react";

// ---- Mock data (swap these for real query results in your app) ----

const NAV_ITEMS = [
    { id: "learn", label: "Learn", icon: BookOpen, active: true },
    { id: "practice", label: "Practice", icon: Dumbbell },
    { id: "leaderboard", label: "Leaderboard", icon: Award },
    { id: "quests", label: "Quests", icon: Target },
    { id: "shop", label: "Shop", icon: ShoppingBag },
    { id: "profile", label: "Profile", icon: Users },
];

const PATH_NODES = [
    { id: 1, kind: "current", icon: Star, shift: 0, label: "Greetings" },
    { id: 2, kind: "locked", icon: Star, shift: -128, label: "Family" },
    { id: 3, kind: "bonus", icon: Sparkles, shift: -96, label: "Bonus round" },
    { id: 4, kind: "locked", icon: Star, shift: -40, label: "Food" },
    { id: 5, kind: "locked", icon: Star, shift: 24, label: "Travel" },
    { id: 6, kind: "trophy", icon: Trophy, shift: 0, label: "Unit checkpoint" },
];

const LEADERBOARD = [
    { rank: 1, name: "Maren K.", xp: 2840 },
    { rank: 2, name: "Theo P.", xp: 2615 },
    { rank: 3, name: "You", xp: 2420, isUser: true },
];

const QUESTS = [
    { id: 1, label: "Earn 20 XP", progress: 8, goal: 20, icon: Zap, fill: "bg-amber-500" },
    { id: 2, label: "Score 90%+ twice", progress: 1, goal: 2, icon: Target, fill: "bg-emerald-500" },
];

// ---- Original simple mascot illustration (not based on any existing character) ----

function Mascot() {
    return (
        <svg viewBox="0 0 140 140" className="w-20 h-20 lg:w-24 lg:h-24" aria-hidden="true">
            <ellipse cx="70" cy="100" rx="34" ry="26" fill="#F2A93B" />
            <circle cx="70" cy="56" r="30" fill="#F2A93B" />
            <path d="M48 40 Q56 18 70 26 Q84 18 92 40" fill="#3B2A52" />
            <circle cx="60" cy="56" r="4.5" fill="#3B2A52" />
            <circle cx="80" cy="56" r="4.5" fill="#3B2A52" />
            <path d="M58 68 Q70 76 82 68" stroke="#3B2A52" strokeWidth="3" fill="none" strokeLinecap="round" />
            <rect x="86" y="84" width="22" height="34" rx="6" fill="#ffffff" stroke="#E2E8F0" strokeWidth="2" />
            <rect x="90" y="90" width="14" height="18" rx="2" fill="#7DD3FC" />
            <ellipse cx="92" cy="104" rx="14" ry="10" fill="#F2A93B" transform="rotate(-15 92 104)" />
        </svg>
    );
}

function PathNode({ node, isActive, onSelect }) {
    let styles = "bg-slate-200 text-slate-400";
    if (node.kind === "current") styles = "bg-amber-500 text-white";
    if (node.kind === "trophy") styles = "bg-slate-100 text-amber-400 border-4 border-amber-200";
    if (node.kind === "bonus") styles = "bg-slate-100 text-violet-400 border-2 border-violet-200";

    return (
        <div
            className="flex flex-col items-center"
            style={{ transform: `translateX(${node.shift}px)` }}
        >
            {node.kind === "current" && (
                <span className="mb-2 rounded-full border-2 border-amber-500 bg-white px-3 py-1 text-xs font-bold uppercase tracking-wide text-amber-600 shadow-sm">
          Start
        </span>
            )}
            <button
                onClick={() => onSelect(node.id)}
                className={`relative w-16 h-16 lg:w-20 lg:h-20 rounded-full flex items-center justify-center shadow-md transition-transform ${styles} ${
                    isActive ? "scale-105" : "hover:scale-105"
                }`}
            >
                {node.kind === "current" && (
                    <span className="absolute inset-0 rounded-full bg-amber-300 animate-ping opacity-60" />
                )}
                <node.icon className="relative w-7 h-7 lg:w-8 lg:h-8" strokeWidth={2.25} />
            </button>
        </div>
    );
}

function SpacePage() {
    const [activeNodeId, setActiveNodeId] = useState(1);
    const activeNode = PATH_NODES.find((n) => n.id === activeNodeId);

    return (
        <div className="min-h-screen w-full bg-slate-50 flex flex-col lg:flex-row">
            {/* Sidebar nav */}
            <aside className="hidden lg:flex lg:w-60 lg:flex-col border-r border-slate-100 bg-white px-4 py-6">
                <div className="flex items-center gap-2 px-2 mb-8">
                    <div className="w-8 h-8 rounded-xl bg-violet-700" />
                    <span className="font-extrabold text-lg text-slate-800">Pathway</span>
                </div>
                <nav className="flex flex-col gap-1">
                    {NAV_ITEMS.map((item) => (
                        <button
                            key={item.id}
                            className={`flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-bold text-left ${
                                item.active ? "bg-violet-50 text-violet-700" : "text-slate-500 hover:bg-slate-50"
                            }`}
                        >
                            <item.icon className="w-5 h-5" />
                            {item.label}
                        </button>
                    ))}
                </nav>
                <button className="mt-auto flex items-center justify-center w-10 h-10 rounded-full bg-slate-100 text-slate-400 hover:bg-slate-200">
                    <Settings className="w-5 h-5" />
                </button>
            </aside>

            {/* Main content */}
            <main className="flex-1 p-4 lg:p-8 grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Center column: header + path */}
                <div className="lg:col-span-2 flex flex-col gap-6">
                    <div className="bg-violet-700 rounded-3xl px-6 py-5 flex items-center justify-between shadow-md">
                        <div>
                            <button className="flex items-center gap-1 text-violet-200 text-xs font-bold uppercase tracking-wide mb-1">
                                <ChevronLeft className="w-4 h-4" /> Section 1, Unit 4
                            </button>
                            <h1 className="text-white text-xl font-extrabold">Order food at a café</h1>
                        </div>
                        <button className="flex items-center gap-2 bg-violet-600 hover:bg-violet-500 text-white text-sm font-bold px-4 py-2 rounded-xl">
                            <BookOpen className="w-4 h-4" /> Guidebook
                        </button>
                    </div>

                    <div className="flex items-center gap-4">
                        <div className="flex-1 h-px bg-slate-200" />
                        <span className="text-xs font-bold uppercase tracking-wide text-slate-400">
              {activeNode?.label ?? "Current chapter"}
            </span>
                        <div className="flex-1 h-px bg-slate-200" />
                    </div>

                    <div className="flex flex-col items-center gap-10 py-6">
                        {PATH_NODES.map((node, idx) => (
                            <div key={node.id} className="flex items-center justify-center gap-6 w-full">
                                <PathNode node={node} isActive={node.id === activeNodeId} onSelect={setActiveNodeId} />
                                {idx === 0 && <Mascot />}
                            </div>
                        ))}
                    </div>
                </div>

                {/* Right column: stats + cards */}
                <div className="flex flex-col gap-4">
                    <div className="flex items-center justify-end gap-3">
                        <button className="w-9 h-9 rounded-full bg-white border border-slate-200 flex items-center justify-center text-rose-500">
                            <Bell className="w-4 h-4" />
                        </button>
                        <div className="flex items-center gap-1 text-slate-300 font-bold text-sm">
                            <Flame className="w-5 h-5" /> 0
                        </div>
                        <div className="flex items-center gap-1 text-sky-500 font-bold text-sm">
                            <Gem className="w-5 h-5" /> 168
                        </div>
                        <div className="flex items-center gap-1 text-rose-500 font-bold text-sm">
                            <Heart className="w-5 h-5" /> 5
                        </div>
                    </div>

                    <div className="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm">
                        <div className="flex items-center gap-3 mb-2">
                            <div className="w-10 h-10 rounded-xl bg-sky-50 text-sky-500 flex items-center justify-center">
                                <Gem className="w-5 h-5" />
                            </div>
                            <div>
                                <p className="font-extrabold text-slate-800 text-sm">Gems</p>
                                <p className="text-xs text-slate-400">You have 168 gems</p>
                            </div>
                        </div>
                        <button className="text-sm font-bold text-violet-700">Go to shop</button>
                    </div>

                    <button className="w-full bg-violet-700 hover:bg-violet-600 text-white font-extrabold py-3 rounded-full shadow-md">
                        Try Premium
                    </button>

                    <div className="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm">
                        <p className="text-xs font-bold uppercase tracking-wide text-slate-400 mb-2">Leaderboard</p>
                        <p className="font-extrabold text-slate-800 text-sm mb-3">You're holding 3rd place</p>
                        <div className="flex flex-col gap-2 mb-3">
                            {LEADERBOARD.map((row) => (
                                <div
                                    key={row.rank}
                                    className={`flex items-center justify-between text-sm px-2 py-1.5 rounded-lg ${
                                        row.isUser ? "bg-violet-50" : ""
                                    }`}
                                >
                  <span className={`font-bold ${row.isUser ? "text-violet-700" : "text-slate-600"}`}>
                    {row.rank}. {row.name}
                  </span>
                                    <span className="text-slate-400">{row.xp} XP</span>
                                </div>
                            ))}
                        </div>
                        <button className="w-full text-sm font-bold text-violet-700 border border-violet-200 rounded-xl py-2">
                            View leaderboard
                        </button>
                    </div>

                    <div className="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm">
                        <div className="flex items-center justify-between mb-3">
                            <p className="font-extrabold text-slate-800 text-sm">Daily quests</p>
                            <button className="text-xs font-bold text-violet-700">View all</button>
                        </div>
                        <div className="flex flex-col gap-3">
                            {QUESTS.map((q) => (
                                <div key={q.id} className="flex items-center gap-3">
                                    <div className="w-8 h-8 rounded-lg bg-slate-100 text-slate-500 flex items-center justify-center">
                                        <q.icon className="w-4 h-4" />
                                    </div>
                                    <div className="flex-1">
                                        <p className="text-xs font-bold text-slate-600 mb-1">{q.label}</p>
                                        <div className="h-2 rounded-full bg-slate-100">
                                            <div
                                                className={`h-2 rounded-full ${q.fill}`}
                                                style={{ width: `${(q.progress / q.goal) * 100}%` }}
                                            />
                                        </div>
                                    </div>
                                    <span className="text-xs text-slate-400">
                    {q.progress}/{q.goal}
                  </span>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
}

export default SpacePage
