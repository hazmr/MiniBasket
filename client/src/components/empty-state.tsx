import type { LucideIcon } from "lucide-react";
import type { ReactNode } from "react";
import { Empty, EmptyContent, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty";

type Props = {
  icon: LucideIcon;
  title: string;
  description?: string;
  children?: ReactNode;
};

export function EmptyState({ icon: Icon, title, description, children }: Props) {
  return (
    <Empty className="border bg-card py-12">
      <EmptyHeader>
        <EmptyMedia variant="icon">
          <Icon />
        </EmptyMedia>
        <EmptyTitle>{title}</EmptyTitle>
        {description && <EmptyDescription>{description}</EmptyDescription>}
      </EmptyHeader>
      {children && <EmptyContent>{children}</EmptyContent>}
    </Empty>
  );
}
