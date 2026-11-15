"use client";

import { TriangleAlert } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Empty, EmptyContent, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty";

export default function Error({ retry }: { error: unknown; retry: () => void }) {
  return (
    <Empty className="border bg-card py-12">
      <EmptyHeader>
        <EmptyMedia variant="icon"><TriangleAlert /></EmptyMedia>
        <EmptyTitle>Something went wrong</EmptyTitle>
        <EmptyDescription>Is the API running? Start it with ./mvnw spring-boot:run in server/.</EmptyDescription>
      </EmptyHeader>
      <EmptyContent>
        <Button onClick={() => retry()}>Try again</Button>
      </EmptyContent>
    </Empty>
  );
}
