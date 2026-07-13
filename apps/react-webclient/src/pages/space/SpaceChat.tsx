import ChatInput from "@/components/ChatInput.tsx";

interface Message {
    role: "user" | "system";
    message: string;
}

const messages: Message[] = [
    { role: "user", message: "Can you summarize the main findings of the attached research paper?" },
    {
        role: "system",
        message:
            "Sure! The paper focuses on three core findings. First, the proposed model outperforms baseline approaches by a significant margin on the benchmark dataset, achieving a 12% improvement in accuracy. Second, the authors show that this improvement holds consistently across different data distributions, suggesting the method generalizes well rather than overfitting to a specific dataset. Third, they identify a key architectural component responsible for most of the performance gain, which they validate through a series of ablation studies. Overall, the paper argues that this component could be integrated into existing architectures with minimal changes, making the approach broadly applicable rather than requiring a full redesign of current systems.",
    },
    { role: "user", message: "Interesting. Does the paper mention any limitations?" },
    {
        role: "system",
        message:
            "Yes, the authors acknowledge a few limitations. The main one is computational cost: while the method improves accuracy, it also increases training time by roughly 30% compared to the baseline, which could be a barrier for teams with limited compute resources. They also note that their evaluation was limited to English-language datasets, so it's unclear how well the approach generalizes to other languages or multilingual settings. Finally, they mention that the ablation studies, while informative, were only run on a subset of the full benchmark due to time constraints, so some of the component-level conclusions might benefit from further validation on the complete dataset in future work.",
    },
    { role: "user", message: "Got it, thanks!" },
];

function SpaceChat() {
    return (
        <div className="flex flex-col w-full h-full items-center">
            <div className="w-full h-full md:max-w-[80%] lg:max-w-[60%] flex flex-col overflow-hidden p-0">
                <div className="flex-1 overflow-y-auto flex flex-col gap-4 p-4">
                    {messages.map((msg, i) => (
                        <div
                            key={i}
                            className={`flex ${msg.role === "user" ? "justify-end" : "justify-start"}`}
                        >
                            <div
                                className={`max-w-[85%] rounded-2xl px-4 py-2 text-sm whitespace-pre-wrap break-words ${
                                    msg.role === "user"
                                        ? "bg-primary text-primary-foreground"
                                        : "bg-muted"
                                }`}
                            >
                                {msg.message}
                            </div>
                        </div>
                    ))}
                </div>

                <div className="flex p-3 border-t w-full shrink-0 place-content-center">
                    <ChatInput onSubmit={() => {}} models={[{ label: "abc", value: "123" }]} />
                </div>
            </div>
        </div>
    );
}

export default SpaceChat;